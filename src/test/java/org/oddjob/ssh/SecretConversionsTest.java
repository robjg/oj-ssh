package org.oddjob.ssh;

import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.junit.jupiter.api.Test;
import org.oddjob.arooa.convert.ConversionPath;
import org.oddjob.arooa.convert.DefaultConversionRegistry;
import org.oddjob.arooa.convert.convertlets.FileConvertlets;
import org.oddjob.io.FileType;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SecretConversionsTest {

    @Test
    public void testToFilePasswordProvider() {

        DefaultConversionRegistry conversionRegistry = new DefaultConversionRegistry();

        new SecretConversions().registerWith(conversionRegistry);

        ConversionPath<String, FilePasswordProvider> stringPath =
                conversionRegistry.findConversion(String.class, FilePasswordProvider.class);

        assertThat(stringPath.toString(), is("String-SecretProvider-FilePasswordProvider"));

        ConversionPath<InputStream, FilePasswordProvider> isPath =
                conversionRegistry.findConversion(InputStream.class, FilePasswordProvider.class);

        assertThat(isPath.toString(), is("InputStream-SecretProvider-FilePasswordProvider"));
    }

    @Test
    public void testFileConversion() {

        DefaultConversionRegistry conversionRegistry = new DefaultConversionRegistry();

        new SecretConversions().registerWith(conversionRegistry);
        new FileType.Conversions().registerWith(conversionRegistry);
        new FileConvertlets().registerWith(conversionRegistry);

        ConversionPath<FileType, FilePasswordProvider> filePath =
                conversionRegistry.findConversion(FileType.class, FilePasswordProvider.class);

        assertThat(filePath.toString(), is("FileType-File-InputStream-SecretProvider-FilePasswordProvider"));

    }

}