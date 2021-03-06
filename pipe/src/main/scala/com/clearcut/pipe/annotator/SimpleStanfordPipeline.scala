package com.clearcut.pipe.annotator

import edu.stanford.nlp.pipeline.{StanfordCoreNLP, Annotation}
import java.util.Properties
import com.clearcut.pipe.model._

class SimpleStanfordPipeline extends Annotator[(Text), (SentenceOffsets, TokenOffsets, Tokens, Poss, NerTags, Lemmas,
  SentenceDependencies)] {

  //val props = new Properties()
  override def setProperties(p:Properties) {
    super.setProperties(p)
    properties.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner, parse")
    properties.put("parse.maxlen", "100")
    properties.put("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz")
    properties.put("threads", "1") // Should use extractor-level parallelism
    properties.put("clean.allowflawedxml", "true")
    properties.put("clean.sentenceendingtags", "p|br|div|li|ul|ol|h1|h2|h3|h4|h5|blockquote|section|article")
  }

  @transient lazy val pipeline = new StanfordCoreNLP(properties)

  override def annotate(t:Text):(SentenceOffsets, TokenOffsets, Tokens, Poss, NerTags, Lemmas, SentenceDependencies) = {
    // Temporary fix for bug where brackets are being incorrectly treated as punct
    // and somehow this messes up the whole dep parse -> change them to round braces
    val text = t.replaceAll( """\[""", "(").replaceAll( """\]""", ")")

    val stanAnn = new Annotation(text)
    pipeline.annotate(stanAnn)

    val (toa, to) = StanfordTokenizer.fromStanford(stanAnn)
    val poss = StanfordPOSTagger.fromStanford(stanAnn)
    val nertags = StanfordNERTagger.fromStanford(stanAnn)
    val lemmas = StanfordLemmatizer.fromStanford(stanAnn)
    val deps = StanfordDependencyExtractor.fromStanford(stanAnn)
    val (so, sto) = StanfordSentenceSplitter.fromStanford(stanAnn)

    (so, toa, to, poss, nertags, lemmas, deps)
  }
}

