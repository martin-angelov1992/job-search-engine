package marto.job_search_engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

public class FillTestJobs {
	private static final Storage STORAGE = new Storage();
	private static final Map<String, List<String>> AVAILABLE_CATEGORY_TYPES = new HashMap<String, List<String>>(){{
		put("employment_type", new LinkedList<String>(){{
			add("Full Time");
			add("Part time");
		}});		
		put("city", new LinkedList<String>(){{
			add("София");
			add("Враца");
			add("Видин");
		}});
//		put("Сектор", new LinkedList<String>(){{
//			add("ИТ");
//			add("Продажби");
//			add("Сив");
//		}});
		put("hierarchy_level", new LinkedList<String>(){{
			add("Ниско");
			add("Високо");
			add("Средно");
		}});
	}};

	private static final List<Job> TEST_JOBS = new LinkedList<Job>(){{
		for (int i=0;i<50;++i) {
			add(new Job("Company 1", "Java", "Requirements: None"));
			add(new Job("Another one", "C++", "Requirements: 3 years"));
			add(new Job("Some Company", ".NET", "Requirements: C++"));
			add(new Job("Strange Company", "PHP", "Requirements: SQL, PHP"));
			add(new Job("BG one", "Seller", "Low salary"));
			add(new Job("Speedy", "Driver", "Driving license required"));
			add(new Job("Kaufland", "Security Guard", "Join us!"));
			add(new Job("Billa", "Security Guard", "Free parking!"));
			add(new Job("Durjavna Rabota", "Administrator", "Word, Excel required"));
			add(new Job("Outsource one", "Manager", "Lying skills required"));
		}
	}};

	public static void main(String[] args) {
		for (Job job : TEST_JOBS) {
			STORAGE.processJobWithKnownFormat(job.categoryTypes, job.company, job.jobTitle, job.text);
		}
	}

	private static class Job {
		private String company;
		private String jobTitle;
		private String text;
		private Map<String, String> categoryTypes;

		public Job(String company, String jobTitle, String text) {
			this.company = company;
			this.jobTitle = jobTitle;
			this.text = text;
			categoryTypes = getRandomCategoryTypes();
		}
	}

	public static Map<String, String> getRandomCategoryTypes() {
		Map<String, String> categoryTypes = new HashMap<String, String>();
		
		int size = ThreadLocalRandom.current().nextInt(1, AVAILABLE_CATEGORY_TYPES.size());

		int counter = 0;
		
		for (Entry<String, List<String>> entry : AVAILABLE_CATEGORY_TYPES.entrySet()) {
			List<String> values = entry.getValue();
			
			int index = ThreadLocalRandom.current().nextInt(0, values.size());

			categoryTypes.put(entry.getKey(), values.get(index));

			if (counter == size) {
				break;
			}
			++counter;
		}

		return categoryTypes;
	}
}