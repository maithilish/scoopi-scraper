package org.codetab.scoopi.step.parse;

import static org.apache.commons.lang3.Validate.validState;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.defs.IItemDef;

/**
 * Data factory
 * @author Maithilish
 *
 */
public class IndexerFactory {

    static final Logger LOG = LogManager.getLogger();

    @Inject
    private IItemDef itemDef;

    @Inject
    private IndexerFactory() {
    }

    public Indexer createIndexer(final String dataDef,
            final List<String> itemNames) {
        validState((itemNames.indexOf("fact") == itemNames.size() - 1),
                "fact should be last item of the list");

        Indexer indexer = new Indexer();
        for (String itemName : itemNames) {
            Range<Integer> range = itemDef.getIndexRange(dataDef, itemName);
            indexer.addIndex(itemName, range);
        }
        indexer.init();
        return indexer;
    }

}
