import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.equalTo;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreateUserTest {

    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }


    @Test
    public void createNewUserTest() {

        User user = new User(((RandomStringUtils.randomAlphanumeric(5))+"@mail.ru"), "ged3459ghe9", "Макс");
        Response response =
                given()
                    .header("Content-type", "application/json")
                    .and()
                    .body(user)
                    .when()
                    .post("/api/auth/register");

        response.then().assertThat().body("success",equalTo(true))
                .and()
                .statusCode(200);
        accessToken = response.jsonPath().getString("accessToken");

    


    @Test
    public void createExistingUserTest() {

        User user = new User("test234t3150ghd@mail.ru", "ged3459ghe9", "Макс");
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(user)
                        .when()
                        .post("/api/auth/register");

        Response response2 =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(user)
                        .when()
                        .post("/api/auth/register");

        response2.then().assertThat().body("success",equalTo(false))
                .and()
                .statusCode(403);
        accessToken = response.jsonPath().getString("accessToken");
    }

    @Test
    public void createNewUserWithoutFieldTest() {

        User user = new User(((RandomStringUtils.randomAlphanumeric(5))+"@mail.ru"), "ged3459ghe9");
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(user)
                        .when()
                        .post("/api/auth/register");

        response.then().assertThat().body("success",equalTo(false))
                .and()
                .statusCode(403);
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
        given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken.replace("Bearer ", ""))
                .when()
                .delete("/api/auth/user")
                .then()
                .assertThat().statusCode(202);
        }
    }

}

