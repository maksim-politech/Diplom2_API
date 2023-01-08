import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class OrdersTest {
    public List<String> ingredients = new ArrayList<>();
    private String ingredient1;
    private String ingredient2;
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

        Response ingredients =
                given()
                        .header("Content-type", "application/json")
                        .get("/api/ingredients");

        System.out.println(ingredients.body().asString());

        ingredient1 = ingredients.jsonPath().getString("data[0]._id");
        ingredient2 = ingredients.jsonPath().getString("data[1]._id");

        try {Thread.sleep(1000);}
        catch (Exception e){}
    }

    @Test
    public void createOrderTest() {
        ingredients.add(ingredient1);
        ingredients.add(ingredient2);
        Order order = new Order(ingredients);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(accessToken.replace("Bearer ", ""))
                        .and()
                        .body(order)
                        .when()
                        .post("/api/orders");
        response.then()
                .assertThat()
                .body("success", equalTo(true))
                .and()
                .statusCode(200);
        System.out.println(response.body().asString());
    }

    @Test
    public void createOrderWithoutAuthTest() {
        ingredients.add(ingredient1);
        ingredients.add(ingredient2);
        Order order = new Order(ingredients);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(order)
                        .when()
                        .post("/api/orders");
        response.then()
                .assertThat()
                .body("success", equalTo(true))
                .and()
                .statusCode(200);
        System.out.println(response.body().asString());
    }


    @Test
    public void createOrderWithoutIngredientsTest() {
        Order order = new Order(ingredients);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(accessToken.replace("Bearer ", ""))
                        .and()
                        .body(order)
                        .when()
                        .post("/api/orders");
        response.then()
                .assertThat()
                .body("success", equalTo(false))
                .and()
                .statusCode(400);
        System.out.println(response.body().asString());
    }

    @Test
    public void createOrderWithInvalidHashTest() {
        ingredients.add("invalid_hash_1");
        ingredients.add("invalid_hash_2");
        Order order = new Order(ingredients);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(accessToken.replace("Bearer ", ""))
                        .and()
                        .body(order)
                        .when()
                        .post("/api/orders");
        response.then()
                .statusCode(500);
        System.out.println(response.body().asString());
    }

    @Test
    public void checkOrderListTest() {
        ingredients.add(ingredient1);
        ingredients.add(ingredient2);
        Order order = new Order(ingredients);
        Response orderResponse =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(accessToken.replace("Bearer ", ""))
                        .and()
                        .body(order)
                        .when()
                        .post("/api/orders");
        orderResponse.then()
                .assertThat()
                .body("success", equalTo(true));

        String orderId = orderResponse.jsonPath().getString("order._id");
        int orderNumber = orderResponse.jsonPath().getInt("order.number");

        Response orderListResponse =
                given()
                        .header("Content-type", "application/json")
                        .auth().oauth2(accessToken.replace("Bearer ", ""))
                        .get("/api/orders");
        orderListResponse.then()
                .assertThat()
                .body("success", equalTo(true))
                .and()
                .statusCode(200);

        System.out.println(orderListResponse.body().asString());
    }

    @Test
    public void checkOrderListWithoutAuthTest() {
    Response orderListResponse =
            given()
                    .header("Content-type", "application/json")
                    .get("/api/orders");
        orderListResponse.then()
                .assertThat()
                .body("success", equalTo(false))
            .and()
                .statusCode(401);
        System.out.println(orderListResponse.body().asString());
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
