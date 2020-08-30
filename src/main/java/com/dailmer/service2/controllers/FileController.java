package com.dailmer.service2.controllers;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dailmer.service2.services.SaveFileService;

import reactor.core.publisher.Mono;

@Controller
public class FileController {
	@Autowired
	SaveFileService saveFileService;

	/**
	 * Mapping to save data in file
	 * 
	 * @param employee
	 * @return Saved FileName
	 * @throws IOException
	 */
	@MessageMapping("Save.File")
	public Publisher<String> saveFileData(byte[] employee) {

		return Mono.justOrEmpty(saveFileService.saveFile(employee));
	}

	/**
	 * Mapping to Update File Data
	 * 
	 * @param employee
	 * @return
	 */
	@MessageMapping("Update.File")
	public Publisher<String> updateFileData(byte[] employee) {

		return Mono.justOrEmpty(saveFileService.updateFile(employee));
	}

	/**
	 * Controller to get Data stored in File
	 * 
	 * @param request
	 * @return File Data in Google Protocol Buffer with Encryption
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	@RequestMapping(value = "/getFileData", consumes = "application/json", produces = "application/x-protobuf", method = RequestMethod.PUT)
	public ResponseEntity<Object> getFileData(@RequestBody Map<String, Object> request)
			throws IOException, ParserConfigurationException {

		return new ResponseEntity<>(saveFileService.getFileData(request), HttpStatus.OK);
	}

	/*
	 * @RequestMapping(value = "/storeFile", consumes = "application/x-protobuf",
	 * produces = "text/plain", method = RequestMethod.PUT) public
	 * ResponseEntity<Object> getAllEmployee(@RequestBody byte[] employee) throws
	 * IOException {
	 * 
	 * return new ResponseEntity<>(saveFileService.saveFile(employee),
	 * HttpStatus.OK); }
	 */

}
