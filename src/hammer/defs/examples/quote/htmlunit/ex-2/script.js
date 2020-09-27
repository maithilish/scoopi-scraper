function execute(webDriver) {

	var Select = Java.type('org.openqa.selenium.support.ui.Select');
	var By = Java.type('org.openqa.selenium.By');

	var pagesToScroll = 4;

	while (true) {
		// scroll
		webDriver
				.executeScript("window.scrollTo(0, document.body.scrollHeight)");
		// wait
		webDriver
				.executeAsyncScript("window.setTimeout(arguments[arguments.length - 1], 500);");

		var eles = webDriver.findElements(By.className("quote"));
		if (eles.size() >= pagesToScroll * 10) {
			break;
		}
	}
}
