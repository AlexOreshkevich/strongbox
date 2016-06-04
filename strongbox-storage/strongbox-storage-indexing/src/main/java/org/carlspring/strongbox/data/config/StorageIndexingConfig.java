package org.carlspring.strongbox.data.config;

import org.apache.maven.index.*;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.artifact.DefaultArtifactPackagingMapper;
import org.apache.maven.index.creator.AbstractIndexCreator;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
@ComponentScan({
        "org.carlspring.strongbox.booters",
        "org.carlspring.strongbox.configuration",
        "org.carlspring.strongbox.services",
        "org.carlspring.strongbox.storage",
        "org.carlspring.strongbox.util"
})
public class StorageIndexingConfig
{

    @Bean(name = "indexer")
    Indexer indexer()
    {
        return new DefaultIndexer(searchEngine(), indexerEngine(), queryCreator());
    }

    @Bean(name = "scanner")
    Scanner scanner()
    {
        return new DefaultScanner(artifactContextProducer());
    }

    @Bean(name = "searchEngine")
    SearchEngine searchEngine()
    {
        return new DefaultSearchEngine();
    }

    @Bean(name = "indexerEngine")
    IndexerEngine indexerEngine()
    {
        return new DefaultIndexerEngine();
    }

    @Bean(name = "queryCreator")
    QueryCreator queryCreator()
    {
        return new DefaultQueryCreator();
    }

    @Bean(name = "artifactContextProducer")
    ArtifactContextProducer artifactContextProducer()
    {
        return new DefaultArtifactContextProducer(artifactPackagingMapper());
    }

    @Bean(name = "artifactPackagingMapper")
    ArtifactPackagingMapper artifactPackagingMapper()
    {
        return new DefaultArtifactPackagingMapper();
    }

    @Bean
    MinimalArtifactInfoIndexCreator minimalArtifactInfoIndexCreator()
    {
        return new MinimalArtifactInfoIndexCreator();
    }

    @Bean
    JarFileContentsIndexCreator jarFileContentsIndexCreator()
    {
        return new JarFileContentsIndexCreator();
    }

    @Bean
    MavenPluginArtifactInfoIndexCreator mavenPluginArtifactInfoIndexCreator()
    {
        return new MavenPluginArtifactInfoIndexCreator();
    }

    @Bean(name = "indexers")
    LinkedHashMap<String, AbstractIndexCreator> indexers()
    {
        LinkedHashMap<String, AbstractIndexCreator> indexers = new LinkedHashMap<>();
        indexers.put("min", minimalArtifactInfoIndexCreator());
        indexers.put("jarContent", jarFileContentsIndexCreator());
        indexers.put("maven-plugin", mavenPluginArtifactInfoIndexCreator());

        return indexers;
    }

}
