package com.devsuperior.dsmovie.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dsmovie.tests.TokenUtil;

import io.restassured.http.ContentType;

public class ScoreControllerRA {
	
	private String clientUsername, clientPassword;
	private String clientToken;
	
	private Map<String, Object> putScoreInstance;
	
	@BeforeEach
	public void setUp() {
		
		baseURI = "http://localhost:8080";
		
		clientUsername = "alex@gmail.com";
		clientPassword = "123456";
		
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		
		putScoreInstance = new HashMap<>();
		putScoreInstance.put("movieId", 1);
		putScoreInstance.put("score", 4);
	}	
	
	@Test
	public void saveScoreShouldReturnOkWhenValidData() {
	   
		JSONObject score = new JSONObject(putScoreInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(score)
		.when()
			.put("/scores")
		.then()
			.statusCode(200)
			.body("title", equalTo("The Witcher"))
			.body("score", is(4.33F))
			.body("count", is(3))
			.body("image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"));
	}
	
	@Test
	public void saveScoreShouldReturnNotFoundWhenMovieIdDoesNotExist() throws Exception {	
		
		putScoreInstance.put("movieId", 1000L);
		JSONObject score = new JSONObject(putScoreInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(score)
		.when()
			.put("/scores")
		.then()
			.statusCode(404)
			.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenMissingMovieId() throws Exception {
		
		putScoreInstance.put("movieId", null);
		JSONObject score = new JSONObject(putScoreInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(score)
		.when()
			.put("/scores")
		.then()
			.statusCode(422)
			.body("error", equalTo("Dados inválidos"))
			.body("errors.message", hasItems("Required field"));
	}
	
	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenScoreIsLessThanZero() throws Exception {	
		
		putScoreInstance.put("score", -1.0);
		JSONObject score = new JSONObject(putScoreInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(score)
		.when()
			.put("/scores")
		.then()
			.statusCode(422)
			.body("error", equalTo("Dados inválidos"))
			.body("errors.message", hasItems("Score should be greater than or equal to zero"));
	}
}
