package com.alstarsoft.tinybrowser.jsparser;

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
@Suite.SuiteClasses({com.alstarsoft.tinybrowser.jsparser.JSParserTest.class,com.alstarsoft.tinybrowser.jsparser.TokenTest.class,com.alstarsoft.tinybrowser.jsparser.ExpressionTest.class,com.alstarsoft.tinybrowser.jsparser.JSValueTest.class,com.alstarsoft.tinybrowser.jsparser.JSStringTest.class,com.alstarsoft.tinybrowser.jsparser.JSFloatTest.class,com.alstarsoft.tinybrowser.jsparser.JSBoolTest.class,com.alstarsoft.tinybrowser.jsparser.JSIntTest.class,com.alstarsoft.tinybrowser.jsparser.NaNTest.class,com.alstarsoft.tinybrowser.jsparser.JSDateTest.class,com.alstarsoft.tinybrowser.jsparser.JSArrayTest.class,com.alstarsoft.tinybrowser.jsparser.JSObjectTest.class,com.alstarsoft.tinybrowser.jsparser.TypedArrayTest.class})
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