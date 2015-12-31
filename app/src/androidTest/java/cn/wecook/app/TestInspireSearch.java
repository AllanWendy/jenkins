package cn.wecook.app;

import android.test.AndroidTestCase;

import com.wecook.common.modules.database.DataLibraryManager;
import com.wecook.sdk.dbprovider.InspireSearchDataLibrary;
import com.wecook.sdk.dbprovider.RecipeIngredientDataProcessor;
import com.wecook.sdk.dbprovider.tables.RecipeIngredientTable;

/**
 * 启发搜索
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/18/14
 */
public class TestInspireSearch extends AndroidTestCase {
    RecipeIngredientDataProcessor processor;
    InspireSearchDataLibrary library;
    RecipeIngredientTable table;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        library = DataLibraryManager.getDataLibrary(InspireSearchDataLibrary.class);
        table = library.getTable(RecipeIngredientTable.class);
        table.clear();
        processor = new RecipeIngredientDataProcessor(table);
    }

    public void testCreateData() {
        processor.writeToDatabase(getContext(), "recipe_counter.json", "ingredient_object_list.json");
    }

}
