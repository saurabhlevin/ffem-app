package org.akvo;

import org.akvo.caddisfly.diagnostic.DiagnosticTest;
import org.akvo.caddisfly.misc.MiscTest;
import org.akvo.caddisfly.navigation.NavigationTest;
import org.akvo.caddisfly.ui.CalibrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({CalibrationTest.class,
        DiagnosticTest.class,
        MiscTest.class,
        NavigationTest.class})
public class UserInterfaceSuite {
}
