package subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

import javax.print.attribute.standard.Media;

@DisplayName("지하철역 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StationAcceptanceTest {
    /**
     * When 지하철역을 생성하면
     * Then 지하철역이 생성된다
     * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // when
        Map<String, String> params = new HashMap<>();
        params.put("name", "강남역");

        ExtractableResponse<Response> response =
                given().log().all()
                        .body(params)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when().post("/stations")
                        .then().log().all()
                        .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        List<String> stationNames =
                given().log().all()
                        .when().get("/stations")
                        .then().log().all()
                        .extract().jsonPath().getList("name", String.class);
        assertThat(stationNames).containsAnyOf("강남역");
    }

    /**
     * Given 2개의 지하철역을 생성하고
     * When 지하철역 목록을 조회하면
     * Then 2개의 지하철역을 응답 받는다
     */
    // TODO: 지하철역 목록 조회 인수 테스트 메서드 생성
    @DisplayName("지하철을 조회한다.")
    @Test
    void getStations() {
        //given: 2개의 지하철역 생성
        Map<String, String> param1 = Map.of("name", "역삼역");
        Map<String, String> param2 = Map.of("name", "선릉역");

        given().body(param1)
               .contentType(MediaType.APPLICATION_JSON_VALUE).log().all()
               .when().post("/stations")
               .then().log().all();

        given().body(param2)
               .contentType(MediaType.APPLICATION_JSON_VALUE).log().all()
               .when().post("/stations")
               .then().log().all();
        //when
        List<StationResponse> response =
            when().get("/stations").then().log().all().extract().jsonPath().getList(".", StationResponse.class);

        //then
        assertThat(response).hasSize(2);
        assertThat(response).extracting(StationResponse::getName).containsExactly("역삼역", "선릉역");
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    // TODO: 지하철역 제거 인수 테스트 메서드 생성
    @DisplayName("지하철을 삭제한다.")
    @Test
    void deleteStation() {
        //given
        Map<String, String> params = Map.of("name", "왕십리역");
        given().body(params).contentType(MediaType.APPLICATION_JSON_VALUE).when().post("/stations").then().log().all();

        //when
        when()
            .delete("/stations/{id}", 1).then().statusCode(HttpStatus.NO_CONTENT.value()).log().all();

        //then
        when().get("/station/{id}", 1).then().statusCode(HttpStatus.NOT_FOUND.value()).log().all();
    }

}