package com.dailmer.service2.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.omg.CORBA.portable.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.dailmer.service2.models.DataModel;
import com.dailmer.service2.models.EmployeeOuterClass.Employee;
import com.dailmer.service2.models.Employees;
import com.google.protobuf.InvalidProtocolBufferException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

@Service
public class SaveFileService {
	@Value("${encrypt.key}")
	String myKey;

	private static SecretKeySpec secretKey;
	private static byte[] key;

	/**
	 * Method to decrypt file data and save as file
	 * 
	 * @param data
	 * @return FileName
	 * @throws IOException
	 */
	public String saveFile(byte[] data) {

		setKey();
		String response = null;
		Employee employee = null;
		String fileName = null;
		try {
			employee = Employee.parseFrom(decrypt(data));
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if ("CSV".equals(employee.getFileType())) {
			try {
				fileName = writeCSV(employee);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response = "File Saved as " + fileName;
		} else if ("XML".equals(employee.getFileType())) {

			fileName = writeToXML(employee);

			response = "File Saved as " + fileName;
		} else
			response = "{\"status\":\"Wrong File Type(CSV/XML)\"}";

		return response;

	}

	public String updateFile(byte[] data) {
		setKey();
		Employee employee = null;
		try {
			employee = Employee.parseFrom(decrypt(data));
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if ("CSV".equals(employee.getFileType())) {
			try {
				updateCSV(employee);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				updateXML(employee);
			} catch (ParserConfigurationException | JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "{\"status\":\"File Updated\"}";

	}

	/**
	 * Method to Retrive File Data
	 * 
	 * @param request
	 * @return File Data in Google Protocol Buffer Formated with Encryption
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings("null")
	public String getFileData(Map<String, Object> request) throws IOException, ParserConfigurationException {
		String[] er = request.get("fileName").toString().split("\\.");
		System.out.println(request.get("fileName"));

		if (er[1].equalsIgnoreCase("csv"))
			return getDataCSV(request);
		else
			return getDataXMK(request);
	}

	private String getDataXMK(Map<String, Object> request) throws ParserConfigurationException {
		String fileName = request.get("fileName").toString();
		File file = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = null;
		try {
			doc = db.parse(file);
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		NodeList nodeList = doc.getElementsByTagName("dataModel");
		Employee employee = Employee.newBuilder().build();
		for (int itr = 0; itr < nodeList.getLength(); itr++) {
			Node node = nodeList.item(itr);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				employee = employee.toBuilder().setName(eElement.getElementsByTagName("name").item(0).getTextContent())
						.setDob(eElement.getElementsByTagName("dob").item(0).getTextContent())
						.setSalary(eElement.getElementsByTagName("salary").item(0).getTextContent())
						.setAge(eElement.getElementsByTagName("age").item(0).getTextContent()).build();

			}
		}

		setKey();
		String data = encrypt(employee.toByteArray());
		return data;
	}

	private String getDataCSV(Map<String, Object> request) throws IOException {
		String fileName = request.get("fileName").toString();

		Reader reader = null;
		try {
			reader = Files.newBufferedReader(Paths.get(fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CSVReader csvReader = new CSVReader(reader);

		String[] record;
		Employee employee = Employee.newBuilder().build();

		while ((record = csvReader.readNext()) != null) {
			employee = employee.toBuilder().setName(record[0]).setDob(record[1]).setSalary(record[2]).setAge(record[3])
					.build();

		}
		csvReader.close();
		reader.close();
		setKey();
		String data = encrypt(employee.toByteArray());
		return data;
	}

	/**
	 * Method to write data as XML File
	 * 
	 * @param employee
	 * @return FileName
	 */
	private String writeToXML(Employee employee) {
		String fileName = employee.getId() + ".xml";
		Employees employees = new Employees();

		List<DataModel> empList = new ArrayList<>();

		empList.add(new DataModel(employee.getName(), employee.getDob(), employee.getSalary(), employee.getAge()));
		employees.setEmployeeList(empList);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Employees.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			File file = new File(fileName);
			jaxbMarshaller.marshal(employees, file);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return fileName;
	}

	/**
	 * Method to Save Data as CSV File
	 * 
	 * @param employee
	 * @return File Name
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String writeCSV(Employee employee) throws IOException {
		String fileName = employee.getId() + ".csv";
		List<DataModel> data = new ArrayList<>();
		data.add(new DataModel(employee.getName(), employee.getDob(), employee.getSalary(), employee.getAge()));

		FileWriter writer = new FileWriter(fileName);
		ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
		mappingStrategy.setType(DataModel.class);

		String[] columns = new String[] { "name", "dob", "salary", "age" };
		mappingStrategy.setColumnMapping(columns);

		StatefulBeanToCsv<DataModel> btcsv = new StatefulBeanToCsvBuilder<DataModel>(writer)
				.withQuotechar(CSVWriter.NO_QUOTE_CHARACTER).withMappingStrategy(mappingStrategy).withSeparator(',')
				.build();
		try {
			btcsv.write(data);
		} catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.close();

		return fileName;

	}

	/**
	 * / Method to get Key for encryption
	 */
	private void setKey() {

		MessageDigest sha = null;
		try {
			try {
				key = myKey.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to encrypt byte array using Generated Key
	 * 
	 * @param byteArrayToEncrypt
	 * @return encrypted data
	 */
	private byte[] decrypt(byte[] strToDecrypt) {
		try {

			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
		} catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
		}
		return null;
	}

	/**
	 * Method to encrypt byte array using Generated Key
	 * 
	 * @param byteArrayToEncrypt
	 * @return encrypted data
	 */
	private String encrypt(byte[] byteArrayToEncrypt) {

		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(byteArrayToEncrypt));
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}

	private void updateCSV(Employee employee) throws IOException {
		String fileName = employee.getId() + ".csv";

		Reader reader = null;
		try {
			reader = Files.newBufferedReader(Paths.get(fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CSVReader csvReader = new CSVReader(reader);

		String[] record;
		List<DataModel> employees = new ArrayList<>();

		while ((record = csvReader.readNext()) != null) {
			employees.add(new DataModel(record[0], record[1], record[2], record[3]));

		}

		employees.add(new DataModel(employee.getName(), employee.getDob(), employee.getSalary(), employee.getAge()));

		FileWriter writer = new FileWriter(fileName);
		ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
		mappingStrategy.setType(DataModel.class);

		String[] columns = new String[] { "name", "dob", "salary", "age" };
		mappingStrategy.setColumnMapping(columns);

		StatefulBeanToCsv<DataModel> btcsv = new StatefulBeanToCsvBuilder<DataModel>(writer)
				.withQuotechar(CSVWriter.NO_QUOTE_CHARACTER).withMappingStrategy(mappingStrategy).withSeparator(',')
				.build();
		try {
			btcsv.write(employees);
		} catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.close();

	}

	private String updateXML(Employee employee) throws ParserConfigurationException, JAXBException {
		String fileName = employee.getId() + ".xml";

		File file = new File(fileName);
		/*
		 * DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 * DocumentBuilder db = dbf.newDocumentBuilder(); Document doc = null; try { doc
		 * = db.parse(file); } catch (SAXException | IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 * doc.getDocumentElement().normalize(); NodeList nodeList =
		 * doc.getElementsByTagName("dataModel"); Employees employees = new Employees();
		 * List<DataModel> empList = new ArrayList<>(); for (int itr = 0; itr <
		 * nodeList.getLength(); itr++) { Node node = nodeList.item(itr); if
		 * (node.getNodeType() == Node.ELEMENT_NODE) { Element eElement = (Element)
		 * node;
		 * 
		 * empList.add((new
		 * DataModel(eElement.getElementsByTagName("name").item(0).getTextContent(),
		 * eElement.getElementsByTagName("dob").item(0).getTextContent(),
		 * eElement.getElementsByTagName("salary").item(0).getTextContent(),
		 * eElement.getElementsByTagName("age").item(0).getTextContent())));
		 * 
		 * } }
		 */

		JAXBContext jaxbContext = JAXBContext.newInstance(Employees.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		Employees emps = (Employees) jaxbUnmarshaller.unmarshal(file);
		Employees employees = new Employees();
		List<DataModel> list = new ArrayList<>();
		for (DataModel emp : emps.getEmployeeList()) {
			list.add(emp);
		}
		employees.setEmployeeList(list);
		System.out.println(employees);
		employees.getEmployeeList()
				.add(new DataModel(employee.getName(), employee.getDob(), employee.getSalary(), employee.getAge()));
		System.out.println(employees);
		try {
			JAXBContext jaxbContextw = JAXBContext.newInstance(Employees.class);
			Marshaller jaxbMarshaller = jaxbContextw.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			jaxbMarshaller.marshal(employees, file);
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		setKey();
		String data = encrypt(employee.toByteArray());
		return data;
	}

}
