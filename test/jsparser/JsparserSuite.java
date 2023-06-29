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
@Suite.SuiteClasses({jsparser.JSParserTest.class,jsparser.TokenTest.class,jsparser.ExpressionTest.class,jsparser.JSValueTest.class,jsparser.JSStringTest.class,jsparser.JSFloatTest.class,jsparser.JSBoolTest.class,jsparser.JSIntTest.class,jsparser.NaNTest.class,jsparser.JSDateTest.class,jsparser.JSArrayTest.class,jsparser.JSObjectTest.class,jsparser.TypedArrayTest.class})
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