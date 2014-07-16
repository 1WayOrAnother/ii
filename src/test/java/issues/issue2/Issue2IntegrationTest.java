package issues.issue2;

import org.ayfaar.app.IntegrationTest;
import org.ayfaar.app.dao.ItemDao;
import org.ayfaar.app.model.Item;
import org.ayfaar.app.utils.ItemsCleaner;
import org.hibernate.criterion.MatchMode;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Issue2IntegrationTest extends IntegrationTest {

    @Value("#{T(org.apache.commons.io.FileUtils).readFileToString(" +
            "T(org.springframework.util.ResourceUtils).getFile('classpath:issues/issue2/clean-item-3.0089.txt')" +
            ")}")
    String itemExpectedContent;

    @Autowired ItemDao itemDao;

    @Test
    public void checkParticularItem() {
        Item item = itemDao.getByNumber("3.0089");
        assertEquals(itemExpectedContent, item.getContent());
    }


    //clean DB of extra chapters and sections
    @Test
    @Ignore
    public void cleanDBFromChaptersAndSections() {
        cleanDB(itemDao);
    }

    @Test
    public void isNotContainChapter() {
        String wrongValue = "\nГлава";

        List<Item> items = itemDao.getLike("content", wrongValue, MatchMode.ANYWHERE);
        assertTrue("Items contain " + items.size() + " elements ",  items.isEmpty());
    }

    @Test
     public void isNotContainSection1() {
        String wrongValue = "\nРаздел";

        List<Item> items = itemDao.getLike("content", wrongValue, MatchMode.ANYWHERE);
        assertTrue("Items contain " + items.size() + " elements ",  items.isEmpty());
    }

    @Test
    public void isNotContainSection2() {
        String wrongValue = "РАЗДЕЛ\n";

        List<Item> items = itemDao.getLike("content", wrongValue, MatchMode.ANYWHERE);
        assertTrue("Items contain " + items.size() + " elements ",  items.isEmpty());
    }

    private void cleanDB(ItemDao dao) {
        List<Item> items = dao.getAll();
        for(Item item : items) {
            item.setContent(ItemsCleaner.clean(item.getContent()));
            dao.save(item);
        }
    }



    /*@Test
    public void isNotContainSection() {
        String wrongValue = "РАЗДЕЛ";
        // аналогично
        List<Item> items = itemDao.getAll();

        for(Item item : items) {
            assertFalse("Item "+item.getNumber()+" has "+wrongValue, isContainIgnoreCase(item.getContent(), wrongValue));
        }
    }*/

    /*private boolean isContain(String itemContext, String value) {
        return itemContext.contains(value);
    }*/

    /*private boolean isContainIgnoreCase(String itemContext, String value) {
        return itemContext.toLowerCase().contains(value.toLowerCase());
    }*/
}
