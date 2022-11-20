
image_name="tb-app"
image_tag_name="latest"
repo_name="orgtboot/trading-boot"
exit_status=0

if [ "$1" ]; then
  image_tag_name=$1
else
  image_tag_name="latest"
fi

#step 1
echo "[INFO] build new package"
mvn clean package -D skipTests

#step 2
exit_status=$?
if [ $exit_status -eq 0 ];
then
  echo "[INFO] build docker image: $image_name"
  docker build . -t $image_name:$image_tag_name -f docker/Dockerfile
  exit_status=$?
else
  echo "[ERROR] failure at step 1"
  exit 1
fi

#step 3
if [ $exit_status -eq 0 ];
then
  echo "[INFO] login into GitHub Container Repository"
  echo $CR_PAT | docker login ghcr.io -u $GH_USER --password-stdin
  exit_status=$?
else
  echo "[ERROR] failure at step 2"
  exit 1
fi

#step 4
if [ $exit_status -eq 0 ];
then
  echo "[INFO] push docker image to $repo_name/$image_name:$image_tag_name"
  docker tag $image_name:$image_tag_name ghcr.io/$repo_name/$image_name:$image_tag_name
  docker push ghcr.io/$repo_name/$image_name:$image_tag_name
  exit_status=$?
else
  echo "[ERROR] failure at step 3"
  exit 1
fi