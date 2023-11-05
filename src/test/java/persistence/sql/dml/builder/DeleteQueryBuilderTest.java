package persistence.sql.dml.builder;

import fixtures.EntityFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import persistence.entity.attribute.EntityAttribute;

import java.util.HashSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Nested
@DisplayName("DeleteQueryBuilder 클래스의")
public class DeleteQueryBuilderTest {
    @Nested
    @DisplayName("prepareStatement 클래스의")
    public class prepareStatement {
        @Nested
        @DisplayName("클래스정보와 이이디값이 주어지면")
        public class withValidArgs {
            @Test
            @DisplayName("적절한 DML을 반환한다.")
            void returnDML() {
                //given
                DeleteQueryBuilder deleteQueryBuilder = new DeleteQueryBuilder();

                //then
                String dml
                        = deleteQueryBuilder.prepareStatement(EntityAttribute.of(EntityFixtures.SampleTwoWithValidAnnotation.class,new HashSet<>()), String.valueOf(1));

                //then
                assertThat(dml).isEqualTo("DELETE FROM two where id = 1");
            }
        }
    }
}
