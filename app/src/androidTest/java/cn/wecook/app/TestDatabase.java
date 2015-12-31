package cn.wecook.app;

import android.test.AndroidTestCase;

import com.wecook.common.modules.database.DataLibraryManager;
import com.wecook.sdk.dbprovider.AppDataLibrary;
import com.wecook.sdk.dbprovider.tables.RecipeTable;

import java.util.List;

/**
 * 测试数据库
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public class TestDatabase extends AndroidTestCase {

    AppDataLibrary appDataLibrary;
    RecipeTable recipeTable;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        appDataLibrary = DataLibraryManager.getDataLibrary(AppDataLibrary.class);
        recipeTable = appDataLibrary.getTable(RecipeTable.class);
        recipeTable.clear();
    }

    public void testDataLibrarySinger() {
        DataLibraryManager.addLibrary(new AppDataLibrary(getContext()));
        AppDataLibrary appData = DataLibraryManager.getDataLibrary(AppDataLibrary.class);
        assertEquals(appData, appDataLibrary);
    }

    public void testDataLibraryAvailable() {
        assertNotNull(appDataLibrary);
        assertNotNull(recipeTable);
    }

    public void testInsert() {
        RecipeTable.RecipeDB recipeDB = new RecipeTable.RecipeDB();
        recipeDB.name = "banana";
        recipeTable.insert(recipeDB);

        List<RecipeTable.RecipeDB> list = recipeTable.query(RecipeTable.NAME + "=?", new String[]{"banana"}, null);
        assertEquals(list.size(), 1);
        RecipeTable.RecipeDB food =list.get(0);
        assertNotNull(food);
        assertEquals(recipeDB.name, food.name);
    }

    public void testDelete() {
        RecipeTable.RecipeDB recipeDB = new RecipeTable.RecipeDB();
        recipeDB.name = "apple";
        recipeTable.insert(recipeDB);

        int result = recipeTable.delete(RecipeTable.NAME + "=?", new String[]{"apple"});
        assertNotSame(result, -1);

        List<RecipeTable.RecipeDB> list = recipeTable.query(RecipeTable.NAME + "=?", new String[]{"apple"}, null);
        assertEquals(list.size(), 0);
    }

    public void testUpdate() {
        RecipeTable.RecipeDB recipeDB = new RecipeTable.RecipeDB();
        recipeDB.name = "orange";
        recipeTable.insert(recipeDB);

        recipeDB.name = "pee";
        int result = recipeTable.update(recipeDB, RecipeTable.NAME + "=?", new String[]{"orange"});
        assertNotSame(result, -1);

        List<RecipeTable.RecipeDB> list = recipeTable.query(RecipeTable.NAME + "=?", new String[]{"pee"}, null);
        assertEquals(list.size(), 1);
    }

}
