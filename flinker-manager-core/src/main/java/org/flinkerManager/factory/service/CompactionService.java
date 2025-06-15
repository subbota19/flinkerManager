package org.flinkerManager.factory.service;

import org.apache.iceberg.RewriteManifests;
import org.apache.iceberg.Table;
import org.apache.iceberg.actions.BaseRewriteDataFilesAction;
import org.apache.iceberg.flink.actions.Actions;
import org.flinkerManager.factory.model.DataCompactionDefinition;
import org.flinkerManager.factory.model.MetadataCompactionDefinition;

public class CompactionService {

    public void compactMetadata(Table table, MetadataCompactionDefinition metadataCompactionDefinition) {

        RewriteManifests rewrite = table.rewriteManifests();
        rewrite.commit();
        System.out.println("Metadata compaction completed.");

    }

    public void compactData(Table table, DataCompactionDefinition dataCompactionDefinition) {

        BaseRewriteDataFilesAction<?> rewriteAction = Actions.forTable(table).rewriteDataFiles();

        Integer targetSizeInBytes = dataCompactionDefinition.getTargetSizeInBytes();
        if (targetSizeInBytes != null) {
            rewriteAction = rewriteAction.targetSizeInBytes(targetSizeInBytes.longValue());
        }

        Integer splitLookback = dataCompactionDefinition.getSplitLookback();
        if (splitLookback != null) {
            rewriteAction = rewriteAction.splitLookback(splitLookback);
        }

        Integer splitOpenFileCost = dataCompactionDefinition.getSplitOpenFileCost();
        if (splitOpenFileCost != null) {
            rewriteAction = rewriteAction.splitOpenFileCost(splitOpenFileCost.longValue());
        }

        rewriteAction.execute();

        System.out.println("Data compaction completed.");
    }

}
