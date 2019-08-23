package com.commercehub.avro.depresolver.avro190;

import com.commercehub.avro.depresolver.SchemaWrapper;
import org.apache.avro.Schema;

class SchemaWrapperImpl extends SchemaWrapper<Schema> {
    private final Schema schema;

    SchemaWrapperImpl(Schema schema) {
        this.schema = schema;
    }

    @Override
    protected String getFullName() {
        return schema.getFullName();
    }

    @Override
    protected String toJson() {
        return schema.toString(true);
    }

    @Override
    public Schema unwrap() {
        return schema;
    }
}