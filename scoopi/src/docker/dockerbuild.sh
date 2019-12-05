name=$1
imageName=$2
dockerFile=$3
releaseZip=$4
version=$5
buildDir=$6

cp $releaseZip $buildDir

mkdir $buildDir/docker
cp $dockerFile $buildDir/docker

cd $buildDir
unzip $releaseZip
mv $name-$version docker/$name

cd docker 
docker build -t $imageName:$version -t $imageName:latest .

