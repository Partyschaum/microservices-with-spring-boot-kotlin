#!/usr/bin/env bash

if [ -d microservices ]; then
  echo "Microservice projects have already been created!"
  exit
fi

mkdir microservices
cd microservices || exit

spring init \
  --boot-version=2.5.4 \
  --build=gradle \
  --language=kotlin \
  --java-version=1.8 \
  --packaging=jar \
  --name=product-service \
  --package-name=com.thatveryfewthings.microservices.core.product \
  --groupId=com.thatveryfewthings.microservices.core.product \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  product-service

spring init \
  --boot-version=2.5.4 \
  --build=gradle \
  --language=kotlin \
  --java-version=1.8 \
  --packaging=jar \
  --name=review-service \
  --package-name=com.thatveryfewthings.microservices.core.review \
  --groupId=com.thatveryfewthings.microservices.core.review \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  review-service

spring init \
  --boot-version=2.5.4 \
  --build=gradle \
  --language=kotlin \
  --java-version=1.8 \
  --packaging=jar \
  --name=recommendation-service \
  --package-name=com.thatveryfewthings.microservices.core.recommendation \
  --groupId=com.thatveryfewthings.microservices.core.recommendation \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  recommendation-service

spring init \
  --boot-version=2.5.4 \
  --build=gradle \
  --language=kotlin \
  --java-version=1.8 \
  --packaging=jar \
  --name=product-composite-service \
  --package-name=com.thatveryfewthings.microservices.composite.product \
  --groupId=com.thatveryfewthings.microservices.composite.product \
  --dependencies=actuator,webflux \
  --version=1.0.0-SNAPSHOT \
  product-composite-service

cd ..
