package be.cytomine.jackson;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import be.cytomine.dto.appengine.task.output.BooleanOutput;
import be.cytomine.dto.appengine.task.output.CollectionOutput;
import be.cytomine.dto.appengine.task.output.DateTimeOutput;
import be.cytomine.dto.appengine.task.output.GeometryOutput;
import be.cytomine.dto.appengine.task.output.IntegerOutput;
import be.cytomine.dto.appengine.task.output.NumberOutput;
import be.cytomine.dto.appengine.task.output.StringOutput;
import be.cytomine.dto.appengine.task.output.TaskRunOutput;

public class CollectionOutputDeserializer extends StdDeserializer<CollectionOutput> {

    public CollectionOutputDeserializer() {
        super(CollectionOutput.class);
    }

    @Override
    public CollectionOutput deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode root = parser.getCodec().readTree(parser);

        UUID taskRunId = root.hasNonNull("taskRunId")
            ? UUID.fromString(root.get("taskRunId").asText())
            : null;
        String parameterName = root.path("parameterName").asText(null);
        String type = root.path("type").asText(null);
        String subType = root.path("subType").asText(null);

        List<CollectionOutput.IndexedTaskRunOutput> items = new ArrayList<>();
        JsonNode valueNode = root.path("value");

        if (valueNode.isArray()) {
            for (JsonNode item : valueNode) {
                if (item.has("type")) {
                    TaskRunOutput output = context.readTreeAsValue(item, TaskRunOutput.class);
                    int index = item.path("index").asInt(-1);
                    items.add(new CollectionOutput.IndexedTaskRunOutput(output, index));
                } else {
                    int index = item.path("index").asInt();
                    JsonNode value = item.path("value");
                    TaskRunOutput output;
                    try {
                        output = parseValue(value, subType, context);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    items.add(new CollectionOutput.IndexedTaskRunOutput(output, index));
                }
            }
        }

        return new CollectionOutput(taskRunId, parameterName, type, items, subType);
    }

    private TaskRunOutput parseValue(JsonNode value, String subType, DeserializationContext context)
        throws IOException, ParseException {
        if (value.isObject()) {
            return context.readTreeAsValue(value, TaskRunOutput.class);
        }

        GeoJsonReader reader = new GeoJsonReader();
        return switch (subType) {
            case "NUMBER" -> new NumberOutput(null, null, "NUMBER", value.doubleValue());
            case "INTEGER" -> new IntegerOutput(null, null, "INTEGER", value.intValue());
            case "BOOLEAN" -> new BooleanOutput(null, null, "BOOLEAN", value.booleanValue());
            case "GEOMETRY" -> new GeometryOutput(null, null, "GEOMETRY", reader.read(value.asText()));
            case "STRING" -> new StringOutput(null, null, "STRING", value.asText());
            case "DATETIME" -> new DateTimeOutput(null, null, "DATETIME", Instant.parse(value.asText()));
            default -> throw new RuntimeException("Unknown subtype: " + subType);
        };
    }
}
