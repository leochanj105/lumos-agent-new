export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
#mvn assembly:assembly
mvn -T 64 $@ clean install -U package
