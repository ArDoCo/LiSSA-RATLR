/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.cli.command;

import static edu.kit.kastel.sdq.lissa.ratlr.cache.CacheManager.DEFAULT_CACHE_DIRECTORY;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.sdq.lissa.ratlr.cache.Cache;
import edu.kit.kastel.sdq.lissa.ratlr.cache.CacheManager;
import picocli.CommandLine;

@CommandLine.Command(
        name = "merge",
        mixinStandardHelpOptions = true,
        description = "Merges cache files of source paths into own default cache or target path")
public class MergeCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MergeCommand.class);

    @CommandLine.Parameters(
            description =
                    "The source paths of the cache files which are used to get the content to merge. If directories are specified, all files in those will be taken. Nothing will be merged into those.")
    private String[] sourcePaths;

    @CommandLine.Option(
            names = {"-t", "--target"},
            converter = CacheManagerConverter.class,
            description =
                    "Sets the target directory to get merged into if another one than the default cache should be used.")
    private CacheManager targetManager = CacheManager.getDefaultInstance();

    @Override
    public void run() {
        for (String sourcePath : this.sourcePaths) {
            Path path = Path.of(sourcePath);
            try {
                if (Files.isDirectory(path)) {
                    // source path is a directory, cache manager of it will be used on all files inside
                    CacheManager sourceManager = new CacheManager(path);
                    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
                        dirStream.forEach(dirEntry -> merge(sourceManager, dirEntry));
                    }
                } else {
                    // source path is a file, cache manager of parent directory is used on that file
                    merge(CacheManagerConverter.getCacheManager(path.getParent()), path);
                }
            } catch (IOException e) {
                logger.warn("Source path '{}' caused an exception while accessing the path: {}", path, e.getMessage());
            }
        }
    }

    private void merge(CacheManager sourceManager, Path... sourcePaths) {
        for (Path path : sourcePaths) {
            if (!Files.isDirectory(path)) {
                try {
                    merge(sourceManager.getCache(path, false), targetManager.getCache(path, true));
                } catch (IllegalArgumentException e) {
                    logger.warn("Source path '{}' caused an exception while merging: {}", path, e.getMessage());
                }
            }
        }
    }

    private void merge(Cache source, Cache target) {
        var overridingKeys = target.addAllEntries(source, true);
        if (!overridingKeys.isEmpty()) {
            logger.warn(
                    "Source file '{}' as it contains {} keys with different value than target cache file",
                    source.getFile().getPath(),
                    overridingKeys.size());
        } else {
            logger.info(
                    "Merged '{}' into '{}'",
                    source.getFile().getPath(),
                    target.getFile().getPath());
        }
    }

    private static class CacheManagerConverter implements CommandLine.ITypeConverter<CacheManager> {

        @Override
        public CacheManager convert(String s) throws IOException {
            return getCacheManager(Path.of(s));
        }

        private static CacheManager getCacheManager(Path cacheDir) throws IOException {
            return cacheDir == null ? new CacheManager(Path.of(DEFAULT_CACHE_DIRECTORY)) : new CacheManager(cacheDir);
        }
    }
}
