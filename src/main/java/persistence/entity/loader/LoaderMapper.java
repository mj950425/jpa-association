package persistence.entity.loader;

import persistence.entity.attribute.EntityAttribute;
import persistence.entity.attribute.EntityAttributes;
import persistence.entity.attribute.GeneralAttribute;
import persistence.entity.attribute.OneToManyField;
import persistence.entity.attribute.id.IdAttribute;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaderMapper {
    private final EntityAttributes entityAttributes;
    private final List<CollectionMapperResolver> COLLECTION_MAPPER_RESOLVERS = new ArrayList<>();

    public LoaderMapper(EntityAttributes entityAttributes, CollectionLoader collectionLoader) {
        COLLECTION_MAPPER_RESOLVERS.add(new EagerLoadingOneToManyFieldMapper(this, entityAttributes));
        COLLECTION_MAPPER_RESOLVERS.add(new LazyLoadingOneToManyFieldMapper(entityAttributes, collectionLoader));

        this.entityAttributes = entityAttributes;
    }

    public <T> T mapResultSetToEntity(Class<T> clazz, ResultSet resultSet) {
        try {
            EntityAttribute entityAttribute = entityAttributes.findEntityAttribute(clazz);
            T instance = instantiateClass(clazz);

            if (!resultSet.next()) {
                return null;
            }

            mapAttributes(entityAttribute, resultSet, instance);

            do {
                mapCollectionAttributes(entityAttribute, resultSet, instance);
            } while (resultSet.next());

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> mapResultSetToList(Class<T> clazz, ResultSet resultSet) {
        Map<String, T> loadedEntities = new HashMap<>();

        EntityAttribute entityAttribute = entityAttributes.findEntityAttribute(clazz);
        try {
            if (!resultSet.next()) {
                return null;
            }

            do {
                String id = String.valueOf(resultSet.getObject(entityAttribute.getIdAttribute().getColumnName()));
                T instance = loadedEntities.get(id);

                if (instance == null) {
                    instance = instantiateClass(clazz);
                    mapAttributes(entityAttribute, resultSet, instance);
                }

                mapCollectionAttributes(entityAttribute, resultSet, instance);
                loadedEntities.put(id, instance);
            } while (resultSet.next());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ArrayList<>(loadedEntities.values());
    }

    public <T> void mapAttributes(EntityAttribute entityAttribute, ResultSet resultSet, T instance) {
        mapIdAttribute(resultSet, instance, entityAttribute.getIdAttribute());
        mapGeneralAttribute(resultSet, instance, entityAttribute.getGeneralAttributes());
    }

    private <T> void mapIdAttribute(ResultSet resultSet, T instance, IdAttribute idAttribute) {
        try {
            Object value = resultSet.getObject(idAttribute.getColumnName());
            idAttribute.getField().setAccessible(true);
            idAttribute.getField().set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void mapGeneralAttribute(ResultSet resultSet, T instance, List<GeneralAttribute> generalAttributes) {
        try {
            for (GeneralAttribute generalAttribute : generalAttributes) {
                Object value = resultSet.getObject(generalAttribute.getColumnName());
                generalAttribute.getField().setAccessible(true);
                generalAttribute.getField().set(instance, value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void mapCollectionAttributes(EntityAttribute entityAttribute, ResultSet resultSet, T instance) {
        for (OneToManyField oneToManyField : entityAttribute.getOneToManyFields()) {
            mapCollectionAttribute(resultSet, instance, oneToManyField);
        }
    }

    private <T> void mapCollectionAttribute(ResultSet resultSet, T instance, OneToManyField oneToManyField) {
        for (CollectionMapperResolver collectionMapperResolver : COLLECTION_MAPPER_RESOLVERS) {
            if (collectionMapperResolver.supports(oneToManyField.getField())) {
                collectionMapperResolver.map(instance, oneToManyField, resultSet);
            }
        }
    }

    private <T> T instantiateClass(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("[%s] 클래스 초기화 실패", clazz.getSimpleName()), e);
        }
    }
}