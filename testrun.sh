export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

export LUMOS_TRACER_DIR=/tmp/
export LUMOS_HADOOP_DIR=/home/jingyuan/hadoop
export LUMOS_TRACING_FRAMEWORK_DIR=/home/jingyuan/tracing-framework/
export TRANSLATION_MAP_PATH=/home/jingyuan/doopstuff/doop/lfacts/translationMap
$JAVA_HOME/bin/java -Dverbose=analysis -DDev=true -Dcomponent=dn -DConcurrencyInst=/home/jingyuan/doopstuff/doop/lfacts/deps/nn/ConcurrencyNondRecord.csv -jar target/LumosAgent.jar
