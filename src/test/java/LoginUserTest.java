import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class LoginUserTest {
    User user = new User(((RandomStringUtils.randomAlphanumeric(5)) + "@mail.ru"), "ged3459ghe9", "Макс");
    String accessToken;
    String correctEmail;
    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        Response response =
            given()
                    .header("Content-type", "application/json")
                    .and()
                    .body(user)
                    .when()
                    .post("/api/auth/register");

        accessToken = response.jsonPath().getString("accessToken");
        correctEmail = response.jsonPath().getString("user.email");

        try {Thread.sleep(1000);}
        catch (Exception e){}
    }

    @Test
    public void loginUserTest() {
        Response response = given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/login");
        response.then().assertThat()
                .body("success", equalTo(true))
                .and()
                .body("accessToken", notNullValue())
                .and()
                .statusCode(200);
        System.out.println(response.body().asString());
    }

    @Test
    public void loginUserWIncorrectEmailTest() {
        User user = new User("test34t4573030ghd0@mail.ru", "ged3459ghe9", "Макс");
        Response response2 = given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/login");
        response2.then().assertThat()
                .body("success", equalTo(false))
                .and()
                .statusCode(401);
    }

    @Test
    public void loginUserWIncorrectPasswordTest() {
        User user = new User(correctEmail, "IncorrectPassword", "Макс");
        Response response2 = given()
                .header("Content-type", "application/json")
                .body(user)
                .post("/api/auth/login");
        response2.then().assertThat()
                .body("success", equalTo(false))
                .and()
                .statusCode(401);
        System.out.println(response2.body().asString());
    }


    @After
    public void deleteUser() {
        given()
                .contentType(ContentType.JSON)
                .auth().oauth2(accessToken.replace("Bearer ", ""))
                .when()
                .delete("/api/auth/user")
                .then()
                .assertThat().statusCode(202);
    }
}
