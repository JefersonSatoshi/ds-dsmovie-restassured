package com.devsuperior.dsmovie.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dsmovie.tests.TokenUtil;

import io.restassured.http.ContentType;

public class MovieControllerRA {
	
	private String title;
	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken, invalidToken;
	private Long existingMovieId, nonExistingMovieId;
	
	private Map<String, Object> postMovieInstance;
	private Map<String, Object> putMovieInstance;
	
	@BeforeEach
	public void setUp() {
		
		baseURI = "http://localhost:8080";
		
		clientUsername = "alex@gmail.com";
		clientPassword = "123456";
		
		adminUsername = "maria@gmail.com";
		adminPassword = "123456";
		
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		invalidToken = adminToken + "xpto";
		
		postMovieInstance = new HashMap<>();
		postMovieInstance.put("title", "Novo filme");
		postMovieInstance.put("score", 3.6);
		postMovieInstance.put("count", 1);
		postMovieInstance.put("image", "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/vIgyYkXkg6NC2whRbYjBD7eb3Er.jpg");
		
		putMovieInstance = new HashMap<>();
		putMovieInstance.put("title", "filme atualizado");
		putMovieInstance.put("score", 4.7);
		putMovieInstance.put("count", 8);
		putMovieInstance.put("image", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
	}
	
	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {
		
		given()
			.get("/movies")
		.then()
			.statusCode(200);
	}
	
	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {		
		
		title = "The Witcher";
		
		given()
			.get("/movies?title={title}", title)
		.then()
			.statusCode(200)
			.body("content[0].id", is(1))
			.body("content[0].title", equalTo("The Witcher"))
			.body("content[0].score", is(4.5F))
			.body("content[0].count", is(2))
			.body("content[0].image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"));
	}
	
	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		
		existingMovieId = 1L;
		
		given()
			.get("/movies/{id}", existingMovieId)
		.then()
			.statusCode(200)
			.body("id", is(1))
			.body("title", equalTo("The Witcher"))
			.body("score", is(4.5F))
			.body("count", is(2))
			.body("image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {	
		
		nonExistingMovieId = 1000L;
		
		given()
			.get("/movies/{id}", nonExistingMovieId)
		.then()
			.statusCode(404)
			.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {		
		
		postMovieInstance.put("title", "");
		JSONObject newMovie = new JSONObject(postMovieInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newMovie)
		.when()
			.post("/movies")
		.then()
			.statusCode(422)
			.body("error", equalTo("Dados inválidos"))
			.body("errors.message", hasItems("Title must be between 5 and 80 characters", "Required field"));
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
		
		JSONObject newMovie = new JSONObject(postMovieInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + clientToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newMovie)
		.when()
			.post("/movies")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
		
		JSONObject newMovie = new JSONObject(postMovieInstance);
		
		given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + invalidToken)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(newMovie)
		.when()
			.post("/movies")
		.then()
			.statusCode(401);
	}
	
	@Test
	public void deleteShouldReturnNoContentWhenAdminLoggedAndIdExists() {
		
		existingMovieId = 29L;
		
		given()
			.header("Authorization", "Bearer " + adminToken)
		.when()
			.delete("/movies/{id}", existingMovieId)
		.then()
			.statusCode(204);
	}

	@Test
	public void deleteShouldReturnNotFoundWhenAdminLoggedAndIdDoesNotExist() {
		
		nonExistingMovieId = 1000L;
		
		given()
			.header("Authorization", "Bearer " + adminToken)
		.when()
			.delete("/movies/{id}", nonExistingMovieId)
		.then()
			.statusCode(404);
	}

	@Test
	public void deleteShouldReturnForbiddenWhenClientLogged() {
		
		existingMovieId = 29L;
		
		given()
			.header("Authorization", "Bearer " + clientToken)
		.when()
			.delete("/movies/{id}", existingMovieId)
		.then()
			.statusCode(403);
	}

	@Test
	public void deleteShouldReturnUnauthorizedWhenInvalidToken() {
		
		existingMovieId = 29L;
		
		given()
			.header("Authorization", "Bearer " + invalidToken)
		.when()
			.delete("/movies/{id}", existingMovieId)
		.then()
			.statusCode(401);
	}
}
