package com.jaguth.fetcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class JobSerializer
{
    private final ObjectMapper mapper = new ObjectMapper();

    public String serialize( final Job job )
    {
        final StringWriter writer = new StringWriter();

        try
        {
            mapper.writeValue( writer, job );

            return writer.toString();
        }
        catch(final IOException ex)
        {
            throw new RuntimeException( ex.getMessage(), ex );
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch (final IOException ex)
            {
                /* Nothing to do here */
            }
        }
    }

    public Job deserialize( final String str )
    {
        final StringReader reader = new StringReader( str );

        try
        {
            return mapper.readValue( reader, Job.class );
        }
        catch( final IOException ex )
        {
            throw new RuntimeException( ex.getMessage(), ex );
        }
        finally
        {
            reader.close();
        }
    }
}
