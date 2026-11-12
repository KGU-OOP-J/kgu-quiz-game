package org.kgu.integration.mgr;

import java.util.Scanner;

public interface Factory {
	public Manageable create(Scanner scan);
}
