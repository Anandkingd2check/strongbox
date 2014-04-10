package org.carlspring.strongbox.storage.indexing;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class LocalRepositoryIndexerTest
{

    private static final File REPOSITORY_BASEDIR = new File("target/strongbox/storages/storage0/releases");

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");

    private static final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.0:jar");


    @Before
    public void init()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        //noinspection ResultOfMethodCallIgnored
        INDEX_DIR.mkdirs();


        ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_BASEDIR.getAbsolutePath(), artifact);
        generator.generate();
    }

    @Test
    public void testIndex() throws Exception
    {
        final LocalRepositoryIndexer i = new LocalRepositoryIndexer("releases", REPOSITORY_BASEDIR, INDEX_DIR);
        try
        {
            System.out.println(REPOSITORY_BASEDIR.getAbsolutePath());
            System.out.println(INDEX_DIR.getAbsolutePath());

            final int x = i.index(REPOSITORY_BASEDIR);
            Assert.assertEquals("Two artifacts were expected!",
                                x,
                                2); // One is a jar, the other -- a pom; both would be added into the same Lucene document

            Set<ArtifactInfo> search = i.search(artifact.getGroupId(), artifact.getArtifactId(), null);
            for (final ArtifactInfo ai : search)
            {
                System.out.println(ai.groupId + " / " + ai.artifactId + " / " + ai.version + " / " + ai.description);
            }

            Assert.assertEquals("Only one " + artifact.getArtifactId() + " artifact was expected!", search.size(), 1);

            i.delete(search);
            search = i.search(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
            Assert.assertEquals(artifact.getArtifactId() + " should have been deleted!", search.size(), 0);
        }
        finally
        {
            i.close(false);
        }
    }

}