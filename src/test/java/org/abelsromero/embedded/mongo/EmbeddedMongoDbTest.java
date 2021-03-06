package org.abelsromero.embedded.mongo;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.abelsromero.embedded.mongo.test.TestAnnotations;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.Description;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;


public class EmbeddedMongoDbTest {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 27017;

    @Test
    public void should_start_a_mongodb_instance() {
        // setup
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newOut));
        // given
        final EmbeddedMongoDb mongo = new EmbeddedMongoDb();
        // when
        mongo.starting(Description.createTestDescription(this.getClass(), "test_name", TestAnnotations.test));
        // then
        assertThat(newOut.toString()).contains("waiting for connections on port");
        // cleanup
        mongo.finished(null);
        System.setOut(originalOut);
    }

    @Test
    public void should_start_a_mongodb_instance_on_a_non_default_port() {
        // setup
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newOut));
        // given
        final EmbeddedMongoDb mongo = new EmbeddedMongoDb();
        // when
        mongo.starting(Description.createTestDescription(this.getClass(), "test_name", new TestAnnotations().nonStandardPortAnnotation()));
        // then
        assertThat(newOut.toString()).contains("waiting for connections on port");
        // cleanup
        mongo.finished(null);
        System.setOut(originalOut);
    }

    @Test
    public void should_not_start_mongodb_instance_if_annotated_with_mongo_skip() {
        // setup
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream newOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newOut));
        // given
        final EmbeddedMongoDb mongo = new EmbeddedMongoDb();
        // when
        mongo.starting(Description.createTestDescription(this.getClass(), "test_name", new TestAnnotations().skinAnnotation()));
        // then
        final String output = newOut.toString();
        assertThat(output).doesNotContain("MongoDB starting");
        assertThat(output).doesNotContain("waiting for connections on port");
        // cleanup
        mongo.finished(null);
        System.setOut(originalOut);
    }

    @Test
    public void should_init_collection_with_1_document_from_json_file() {
        // given
        final EmbeddedMongoDb mongo = new EmbeddedMongoDb();
        // when
        mongo.starting(Description.createTestDescription(this.getClass(), "test_name_init_1", new TestAnnotations().importSingleJsonAnnotation()));
        // then
        final MongoCollection<Document> collection = getDefaultCollection();
        assertThat(collection.count()).isEqualTo(1l);
        // cleanup
        mongo.finished(null);
    }

    @Test
    public void should_init_collection_with_multiple_documents_from_json_file() {
        // given
        final EmbeddedMongoDb mongo = new EmbeddedMongoDb();
        // when
        mongo.starting(Description.createTestDescription(this.getClass(), "test_name", new TestAnnotations().importMultipleJsonAnnotation()));
        // then
        final MongoCollection<Document> collection = getDefaultCollection();
        assertThat(collection.count()).isEqualTo(2l);
        // cleanup
        mongo.finished(null);
    }

    @Test
    public void should_fail_when_importing_documents_in_array_format_with_default_options() {
        // given
        final EmbeddedMongoDb mongo = new EmbeddedMongoDb();
        // when
        try {
            mongo.starting(Description.createTestDescription(this.getClass(), "test_name", new TestAnnotations().importMultipleJsonInArrayAnnotation()));
        } catch (Exception e) {
            assertThat(e.getClass()).isEqualTo(IOException.class);
        } finally {
            // cleanup
            mongo.finished(null);
        }
    }

    @Test
    public void should_init_when_importing_documents_in_array_format_with_jsonArray_set_to_true() {
        // given
        final EmbeddedMongoDb mongo = new EmbeddedMongoDb();
        // when
        mongo.starting(Description.createTestDescription(this.getClass(), "test_name", new TestAnnotations().importMultipleJsonInArrayTrueAnnotation()));
        // then
        final MongoCollection<Document> collection = getDefaultCollection();
        assertThat(collection.count()).isEqualTo(3l);
        // cleanup
        mongo.finished(null);
    }

    @Test
    public void should_fail_when_file_is_not_found() {
        // given
        final EmbeddedMongoDb mongo = new EmbeddedMongoDb();
        // when
        try {
            mongo.starting(Description.createTestDescription(this.getClass(), "test_name", new TestAnnotations().nonExistentFileAnnotation()));
        } catch (Exception e) {
            assertThat(e)
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("Could not find file: non_existent_file.json");
        } finally {
            // cleanup
            mongo.finished(null);
        }
    }


    private MongoCollection<Document> getDefaultCollection() {
        return new MongoClient(HOST, PORT)
            .getDatabase("local")
            .getCollection("embedded-test-collection");
    }

}
