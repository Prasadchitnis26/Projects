import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import twitter4j.TwitterFactory;
import twitter4j.conf.*;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class FirstApp {
	static int marks = 0, gl = 0, bl = 0, pt = 0, nt = 0, finalScore = 0, neutral = 0, wl = 0;
	static String goodL[] = new String[10000];
	static String badL[] = new String[10000];
	static String wordList[][] = new String[10000][2];
	static String bird[];

	public static void main(String args[]) throws IOException, TwitterException {
		makeList();
		ConfigurationBuilder c = new ConfigurationBuilder();
		c.setDebugEnabled(true).setOAuthConsumerKey("someAuthorizationKey")
				.setOAuthConsumerSecret("someAuthConsumerSecret")
				.setOAuthAccessToken("someAuthAccessToken")
				.setOAuthAccessTokenSecret("someAuthAccessTokenSecret");

		TwitterFactory tf = new TwitterFactory(c.build());
		twitter4j.Twitter twit = tf.getInstance();

		List<Status> status = twit.getHomeTimeline();
		for (Status st : status) {
			// System.out.println(st.getUser().getName() + " " + st.getText());
		}
		Twitter t = tf.getInstance();

		Query query = new Query("samsung");//the #hashtag value on which the query is done
		Date date = new Date();
		System.out.println(date);
		String modifiedDate = new SimpleDateFormat("yyyymmdd").format(date);
		query.setSince(modifiedDate);
		QueryResult result;
		do {
			result = t.search(query);//get tweets on the #
			List<Status> tweets = result.getTweets();
			int len = tweets.size();
			bird = new String[len];
			int i = 0;
			for (Status tweet : tweets) {
				System.out.println(len + " " + tweet.getText());
				if (tweet.getText() != null) {
					reviewT(tweet.getText()); // for every tweet perform sentimental analysis
				}
				bird[i++] = tweet.getText();
			}
		} while ((query = result.nextQuery()) != null);
		Score();
	}

	public static void Score() {
		System.out.println("final score by tweets");
		int t = pt + nt;
		float f = finalScore / t;
		System.out.println("The average score in " + t + " tweets is " + f);
		System.out.println("neutral tweets " + neutral);

	}

	public static void makeList() throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader("linkToTheListThatHasAllThePositiveWords"))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				goodL[gl++] = line;
			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader("linkToTheListThatHasAllTheNegativeWords"))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				badL[bl++] = line;
			}
		}
	}
//This method is used to review the score the tweet gets
	public static void reviewT(String tweet) {
		String array[] = tweet.split(" ");
		int wrds = 0;
		for (int i = 0; i < array.length; i++) {
			wordL(array[i]);
			for (int j = 0; j < gl; j++) {
				if (goodL[j].equals(array[i])) {
					wrds++;
				}
			}
			for (int j = 0; j < bl; j++) {
				if (badL[j].equals(array[i])) {
					wrds--;
				}
			}
		}
		if (wrds > 3) {
			finalScore += 5;
			System.out.println(">3");
			pt++;
		} else if (wrds > 0) {
			finalScore += 3;
			System.out.println(">0");
			pt++;
		} else if (wrds < 0) {
			finalScore += 1;
			System.out.println("<0");
			nt++;
		} else if (wrds < -2) {
			// finalScore-=2;
			System.out.println("<-2");
			nt++;
		}
		if (wrds == 0) {
			neutral++;
		}

	}

	public static void wordL(String s) {
		boolean f = false;
		int k=0;
		for (int i = 0; i < wl; i++) {
			if (s.equals(wordList[i][0])) {
				if(wordList[i][1] != null){
				 k = Integer.parseInt(wordList[i][1]);
				}
				wordList[i][1] = (k++) + "";
				f = true;
			}
		}
		if (!f) {
			wordList[wl++][0] = s;
			wordList[wl++][1] = 0 + "";
			f = true;
		}
	}
}
