package com.dmatrix.iot.devices;

import java.util.Properties;
import java.util.Date;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
/**
 * Created by jules on 12/8/15.
 * A simple illustration of how to use basic API to create a simple producer that publishes N number of device related
 * information on a topic "devices."
 *
 * Simple steps:
 *  1. Create a schema for your topic
 *  2. Create a message, in this case my JSON like doc, that adheres to the Avro Schema
 *  3. Verify the schema
 *  4. Create a handle to a Kafka producer
 *  5. Publish it!
 *  This example follows the same structure as the github example https://github.com/confluentinc/examples
 *
 */
public class SimplePublisher {

	private String topic;

	/**
	 *
	 * @param pTopic
     */
	public SimplePublisher(String pTopic)
	{
		topic = pTopic;
	}

	/**
	 *
	 * @return
     */
	public String getTopic()
	{
		return topic;
	}

	/**
	 *
	 * @param id
	 * @param schema
     * @return
     */
	public GenericRecord buildDeviceInfo(int id, Schema schema) {

		GenericRecord deviceRecord = new GenericData.Record(schema);
		deviceRecord.put("device_id", id);
		deviceRecord.put("device_name", IotDevices.getDeviceType(id));
		deviceRecord.put("ip", "192.34.5." + id);
		deviceRecord.put("temp", IotDevices.getTemp());
		deviceRecord.put("humidity", IotDevices.getHumidity());
		deviceRecord.put("lat", IotDevices.getCoordinate());
		deviceRecord.put("long", IotDevices.getCoordinate());
		deviceRecord.put("zipcode", IotDevices.getZipCode(id));
		deviceRecord.put("scale", "Celsius");
		deviceRecord.put("timestamp", new Date().getTime());

		return deviceRecord;

	}
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out
					.println("Usage: SimplePublisher topic number schema-registry-URL");
			System.exit(1);
		}
		String topic = args[0];
		int numOfDevices;
		numOfDevices = Integer.valueOf(args[1]);
		String url= args[2];

		SimplePublisher sp = new SimplePublisher(topic);
		// build properties
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("key.serializer",
				"io.confluent.kafka.serializers.KafkaAvroSerializer");
		props.put("value.serializer",
				"io.confluent.kafka.serializers.KafkaAvroSerializer");
		props.put("schema.registry.url", url);
		// create a schema for the device JSON
		String schemaDeviceString = "{\"namespace\": \"device.avro\", \"type\": \"record\", " +
				"\"name\": \"devices\"," +
				"\"fields\": [" +
				"{\"name\": \"device_id\", \"type\": \"int\"}," +
				"{\"name\": \"device_name\", \"type\": \"string\"}," +
				"{\"name\": \"ip\", \"type\": \"string\"}," +
				"{\"name\": \"temp\", \"type\": \"int\"}," +
				"{\"name\": \"humidity\", \"type\": \"int\"}," +
				"{\"name\": \"lat\", \"type\": \"int\"}," +
				"{\"name\": \"long\", \"type\": \"int\"}," +
				"{\"name\": \"zipcode\", \"type\": \"int\"}," +
				"{\"name\": \"scale\", \"type\": \"string\"}," +
				"{\"name\": \"timestamp\", \"type\": \"long\"}" +
				"]}";
		// check schema
		Schema.Parser parser = new Schema.Parser();
		Schema schema = parser.parse(schemaDeviceString);

		// instantiate a Kafka producer
		Producer<String, GenericRecord> producer = new KafkaProducer<String, GenericRecord>(props);
		// create devices info based on the schema
		for (int i = 0; i < numOfDevices; i++) {
			GenericRecord deviceRec = sp.buildDeviceInfo(i, schema);
			// create a ProducerRecord
			ProducerRecord<String, GenericRecord> data = new ProducerRecord<String, GenericRecord>(topic, deviceRec);
			// publish it on the topic "devices." This assumes that the topic exists
			try {
				System.out.format("Device info publishing to Kafka topic %s : %s\n",
						sp.getTopic(), data.toString()) ;
				producer.send(data);
			} catch (Exception exec) {
				exec.printStackTrace();
			}
		}
		producer.close();
	}

}
