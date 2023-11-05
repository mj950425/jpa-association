package persistence.entity.loader;

import java.lang.reflect.Field;
import java.sql.ResultSet;

public interface CollectionMapperResolver {
    Boolean supports(Field field);

    <T> void map(T instance, Field field, ResultSet resultSet);
}