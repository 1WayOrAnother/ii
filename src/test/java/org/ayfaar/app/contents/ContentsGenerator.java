package org.ayfaar.app.contents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.ayfaar.app.SpringTestConfiguration;
import org.ayfaar.app.utils.CategoryService;
import org.ayfaar.app.utils.contents.CategoryPresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.*;

public class ContentsGenerator {
	private static final Logger logger = LoggerFactory.getLogger(ContentsGenerator.class);

	public static void main(String[] args) throws Exception {
		// задаём профайл для загрузки только нужных бинов
		System.setProperty("spring.profiles.active", ContentsGeneratorConfig.CONTEXT_GENERATOR_PROFILE);
		// создаём контекст тестового окружения
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringTestConfiguration.class);
		// получаем сервис для работы с категориями
		CategoryService categoryService = ctx.getBean(CategoryService.class);
		// движок html шаблонов. Про диалект шаблонов: http://www.thymeleaf.org/doc/articles/standarddialect5minutes.html
		TemplateEngine templateEngine = ctx.getBean(TemplateEngine.class);

		// переменные видимые в шаблоне
		Map<String, Object> values = new HashMap<>();
		// провейдер категории "Том 4"
		CategoryService.CategoryProvider rootCategoryProvider = categoryService.getByName("Том 4");
		logger.trace("Загруженная категория: " + rootCategoryProvider.extractCategoryName());
		// в этом объект нужно положить всю нужную в шаблоне информацию о категрии (имя, описание, дочерние категории...)
		CategoryPresentation rootCategoryPresentation = null; //
		List<CategoryPresentation> categoryPresentationList = fillCategoryPresentationList(rootCategoryProvider.getChildren());
		// заполняем поле name, которое содержит полное название (к примеру - Основы. Том 4)
		String rootCategoryPresentationName;
		rootCategoryPresentationName = rootCategoryProvider.getParentUri().substring(rootCategoryProvider.getParentUri().indexOf(":")+1)+"."+rootCategoryProvider.extractCategoryName();
		rootCategoryPresentation =  new CategoryPresentation(rootCategoryPresentationName,
				rootCategoryProvider.getUri(),rootCategoryProvider.getDescription(), categoryPresentationList);
		// делаем объект категории доступным для шаблона по пути "data"
		values.put("data", rootCategoryPresentation);
		// заполняем шаблон данными и получаем результат в виде html с данными
		String html = templateEngine.process("contents.html", new Context(Locale.getDefault(), values));
//		logger.trace("Результат работы шаблонизатора: \n" + html);
		logger.trace("Генерация шаблона окончена.");
		// записываем полученный результат в html файл для просмотра его в браузере.
		// вариант записи в файл - какой больше понравится

//		FileOutputStream fileOutputStream = new FileOutputStream("html.html");
//		fileOutputStream.write(html.getBytes("UTF-8"));
//		fileOutputStream.flush();
//		fileOutputStream.close();

		FileUtils.writeStringToFile(new File("html.html"), html, Charset.forName("UTF-8"));
	}

	public static List<CategoryPresentation> fillCategoryPresentationList(List<CategoryService.CategoryProvider> rootCategoryProvider){
		// рекурсивно заполняем список сategoryPresentationList
		List<CategoryPresentation> сategoryPresentationList = new ArrayList<>();
		for (CategoryService.CategoryProvider provider : rootCategoryProvider){
			// заполняем поле name, которое содержит название параграфа, главы и т.д.
			String name = provider.extractCategoryName();
			String[] uri_full = name.split(":");
			String[] nameArr = name.split("\\.");
			// если название длинное, то убераем лишнюю часть, если короткое, то значит,
			// это новый Том, или Раздел, и название там не нужно
			if(nameArr.length > 2)
				name = nameArr[nameArr.length-2]+"."+nameArr[nameArr.length-1];

			// рекурсивно заполняем список, со всеми вложенными детьми
			сategoryPresentationList.add(new CategoryPresentationExt(name, provider.getStartItemNumber(),
					provider.getDescription(), fillCategoryPresentationList(provider.getChildren()),uri_full[uri_full.length-1]));
		}
		return сategoryPresentationList;
	}

	/*
	* Этот класс создан для того, что бы формировался
	* полноценный путь для перехода на сайт, на конкретное
	* место. Добавленна ссылка uri_full
	*/

	@Data
	@NoArgsConstructor
	public static class CategoryPresentationExt extends CategoryPresentation{
		/**
		 * Ссылка на место в источнике
		 */
		private String uri_full;
		/**
		 * Здесь мы задаем полную ссылку на место в документации,
		 * с помощью которой формируем путь на сайте.
		 * К примеру - 4.13.1.3
		*/
		public CategoryPresentationExt(String name, String uri, String description, List<CategoryPresentation> children, String uri_full) {
			super(name, uri, description, children);
			this.uri_full = uri_full;
		}


	}
}
