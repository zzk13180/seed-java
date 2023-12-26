#!/bin/sh
cd $PWD/.. || exit
HOME=$PWD
echo "HOME $HOME"
echo "start mvn clean package"
# mvn clean package

dir="$HOME/dist"
if [ ! -d "$dir" ]; then
  mkdir -p "$dir"
fi

cp -f $HOME/bin/app.sh $HOME/dist/app.sh
