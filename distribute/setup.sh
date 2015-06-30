#!/usr/bin/env bash

# install virtualenv
command -v virtualenv >/dev/null 2>&1 || {
  echo >&2 "virtualenv required but not installed. Aborting.";
  echo >&2 "You can install virtualenv with:"
  echo >&2 "    sudo pip install virtualenv"

virtualenv env
source env/bin/activate

pip install azure
pip install botocore
pip install fabric


# install SSH keys
if [ ! -f "./ssh/bazaar.key" ]; then
  echo "Creating SSH keys"
  mkdir ./ssh
  cd ./ssh

  # generate private/public key pair
  ssh-keygen -t rsa -b 2048 -f bazaar.key -N '' -C bazaar

  # generate azure pem file from openssh private key
  openssl req \
    -x509 \
    -days 365 \
    -nodes \
    -key bazaar.key \
    -out bazaar.pem \
    -newkey rsa:2048 \
    -subj "/"
  cd ..
fi

# install (separate) management certificates for azure
cd ssh
`openssl req -x509 -nodes -days 365 -newkey rsa:1024 -keyout mycert.pem -out mycert.pem`
`openssl x509 -inform pem -in mycert.pem -outform der -out mycert.cer`

echo 'NOTE: If you would like to use Azure, you must upload ssh/mycert.cer via the "Upload" action of the "Settings" tab of the management portal.'

