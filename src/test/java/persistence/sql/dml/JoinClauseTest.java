package persistence.sql.dml;

import fixtures.EntityFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import persistence.entity.attribute.EntityAttribute;

import java.util.HashSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Nested
@DisplayName("JoinClause 클래스의")
class JoinClauseTest {
    @Nested
    @DisplayName("생성자는")
    class constructor {
        @Nested
        @DisplayName("연관관계가 맺어진 엔티티의 연관관계 메타정보가 주어지면")
        class withRelationMeta {
            @Test
            @DisplayName("적절한 조인 DML을 제공한다.")
            void returnJoinDML() {
                EntityAttribute entityAttribute = EntityAttribute.of(EntityFixtures.Order.class, new HashSet<>());
                JoinClause joinClause = new JoinClause(entityAttribute.getOneToManyFields());
                assertThat(joinClause.toString()).isEqualTo(" join order_items as order_items on orders.id = order_items.id");
            }
        }
    }
}