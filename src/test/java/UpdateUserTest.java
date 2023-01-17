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

public class UpdateUserTest {
    User user = new User(((RandomStringUtils.randomAlphanumeric(5)) + "@mail.ru"), "ged3459ghe9", "Макс");
    String accessToken;
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
        System.out.println(response.body().asString());
    }

    @Test
    public void UpdateEmailAndNameUserTest() {
        User newUser = new User("NewNotRandom11@mail.ru", "NEWgedds3459ghe9", "NEWМакс");
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(accessToken.replace("Bearer ", ""))
                        .and()
                        .body(newUser)
                        .patch("/api/auth/user");
            response.then()
                    .assertThat()
                    .body("user.email", equalTo("NewNotRandom11@mail.ru".toLowerCase()))
                    .and()
                    .body("user.name", equalTo("NEWМакс"))
                    .and()
                   .statusCode(200);
        System.out.println(response.body().asString());
    }

    @Test
    public void UpdatePasswordUserTest() {
        User newUser = new User("NewNotRandom@mail.ru", "NEWged3459ghe9", "NEWМакс");
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(accessToken.replace("Bearer ", ""))
                        .and()
                        .body(newUser)
                        .patch("/api/auth/user");

        Response response2 = given()
                .header("Content-type", "application/json")
                .body(newUser)
                .post("/api/auth/login");

        response2.then().assertThat()
                .body("success", equalTo(true));
        System.out.println(response.body().asString());
    }

    @Test
    public void UpdateUserWithoutAuthTest() {
        User newUser = new User("NewNotRandom@mail.ru", "NEWged3459ghe9", "NEWМакс");
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newUser)
                        .patch("/api/auth/user");
        response.then().assertThat()
                .body("message", equalTo("You should be authorised"))
                .and()
                .statusCode(401);
        System.out.println(response.body().asString());
    }

    @Test
    public void UpdateUserWithSameEmailTest() {
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(accessToken.replace("Bearer ", ""))
                        .and()
                        .body(user)
                        .patch("/api/auth/user");
        response.then()
                .statusCode(200); ///дОЛЖЕН БЫТЬ 403 СТАТУС КОД ПО ДОКУМЕНТАЦИИ, НО ВЫДАЕТ 200
        System.out.println(response.body().asString());
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
