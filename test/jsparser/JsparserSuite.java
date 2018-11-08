package jsparser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Alex
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({jsparser.JSStringTest.class,jsparser.JSFloatTest.class,jsparser.JSValueTest.class,jsparser.JSIntTest.class,jsparser.ExpressionTest.class,jsparser.NaNTest.class,jsparser.JSParserTest.class,jsparser.TokenTest.class,jsparser.JSBoolTest.class})
public class JsparserSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}