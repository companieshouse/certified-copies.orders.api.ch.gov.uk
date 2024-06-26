package uk.gov.companieshouse.certifiedcopies.orders.api.util;

import jakarta.json.Json;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * HTTP message converter for {@link JsonMergePatch}.
 * <p>
 * Only supports {@code application/merge-patch+json} media type.
 */
@Component
public class JsonMergePatchHttpMessageConverter extends AbstractHttpMessageConverter<JsonMergePatch> {

    public JsonMergePatchHttpMessageConverter() {
        super(PatchMediaType.APPLICATION_MERGE_PATCH);
    }

    @Override
    protected boolean supports(@NonNull Class<?> clazz) {
        return JsonMergePatch.class.isAssignableFrom(clazz);
    }

    @Override
    protected JsonMergePatch readInternal(@NonNull Class<? extends JsonMergePatch> clazz,
                                          @NonNull HttpInputMessage inputMessage) {

        try (JsonReader reader = Json.createReader(inputMessage.getBody())) {
            return Json.createMergePatch(reader.readValue());
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
        }
    }

    @Override
    protected void writeInternal(@NonNull JsonMergePatch jsonMergePatch, @NonNull HttpOutputMessage outputMessage) {

        try (JsonWriter writer = Json.createWriter(outputMessage.getBody())) {
            writer.write(jsonMergePatch.toJsonValue());
        } catch (Exception e) {
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        }
    }
}

