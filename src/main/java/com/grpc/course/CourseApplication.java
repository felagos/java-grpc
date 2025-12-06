package com.grpc.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.grpc.models.Person;
import com.grpc.models.Student;

@SpringBootApplication
public class CourseApplication {

	public static void main(String[] args) {
		var personProton = getPersonProton();
		var personJson = getPersonJson();

		proton(personProton);
		json(personJson);

		/* for (int i = 0; i < 5; i++) {
			try {
				runTests("Proton", () -> proton(personProton));
				runTests("JSON", () -> json(personJson));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} */

	}

	private static Person getPersonProton() {
		return Person.newBuilder()
				.setAge(20)
				.setName("Name")
				.setEmail("email@example.com")
				.build();
	}

	private static PersonJson getPersonJson() {
		return new PersonJson("Name", 20, "email@example.com");
	}

	private static void proton(Person person) throws RuntimeException {
		try {
			var bytes = person.toByteArray();
			System.out.println("Proton Byte size: " + bytes.length);
			Person.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}

	public static void json(PersonJson person) throws RuntimeException {
		var mapper = new ObjectMapper();

		try {
			var bytes = mapper.writeValueAsBytes(person);
			System.out.println("JSON Byte size: " + bytes.length);
			mapper.readValue(bytes, PersonJson.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void runTests(String testName, Runnable runnable) throws Exception {
		var start = System.currentTimeMillis();

		for (int i = 0; i < 1000000; i++) {
			runnable.run();
		}

		var end = System.currentTimeMillis();

		System.out.println(testName + " took " + (end - start) + "ms");
	}
}