#!/bin/bash

image_name="tb-app"
image_tag_name="latest"
repo_name="orgtboot/trading-boot"
exit_status=0

if [ "$1" ]; then
  image_tag_name=$1
fi

#step 1
echo "[INFO] Build new package"
mvn clean package -D skipTests

#step 2
exit_status=$?
if [ $exit_status -eq 0 ]; then
  echo "[INFO] Build docker image: $image_name"
  if [ "$2" ]; then
    docker buildx build . --platform "$2" -t $image_name:"$image_tag_name" -f docker/Dockerfile
  else
    docker build . -t $image_name:"$image_tag_name" -f docker/Dockerfile
  fi
  exit_status=$?
else
  echo "[ERROR] Failure at step 1"
  exit 1
fi

#step 3
if [ $exit_status -eq 0 ]; then
  echo "[INFO] Login into GitHub Container Repository"
  echo "$CR_PAT" | docker login ghcr.io -u "$GH_USER" --password-stdin
  exit_status=$?
else
  echo "[ERROR] Failure at step 2"
  exit 1
fi

#step 4
if [ $exit_status -eq 0 ]; then
  echo "[INFO] Push docker image to $repo_name/$image_name:$image_tag_name"
  docker tag $image_name:"$image_tag_name" ghcr.io/$repo_name/$image_name:"$image_tag_name"
  docker push ghcr.io/$repo_name/$image_name:"$image_tag_name"
  exit_status=$?
else
  echo "[ERROR] Failure at step 3"
  exit 1
fi