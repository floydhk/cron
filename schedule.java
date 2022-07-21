package com.sample.app.tasks;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Scheduled(cron = "0 34-36 17 * * *")
	public void crawlWeb() {

//		ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("* * * * * * *"));
//		CronTrigoger cronTrigger=new CronTrigger("58 * * * * *");
		CronExpression cronTrigger = CronExpression.parse("0 41 11 * * *");

		LocalDateTime next = cronTrigger.next(LocalDateTime.now());


		CronExpression expression = CronExpression.parse("0 34-36 17 * * *");
		ZonedDateTime nextTime = expression.next(ZonedDateTime.now());

		ZonedDateTime prevTime = getPrevTime(expression, nextTime, nextTime.minusDays(1));



		System.out.println("xx "+ cronTrigger.equals(LocalDateTime.now()));
		System.out.println("Next Execution Time: " + next);



		CronTrigger trigger = new CronTrigger("57 * * * * *");
		SimpleTriggerContext triggerContext = new SimpleTriggerContext();
		triggerContext.update(Date.from(ZonedDateTime.now().toInstant()), Date.from(ZonedDateTime.now().toInstant()), Date.from(ZonedDateTime.now().toInstant()));
		Date nextFireAt = trigger.nextExecutionTime(triggerContext);

		System.out.println(nextFireAt);
	//	CronExpression cron = new CronExpression("0 15 10 ? * *");
		Date today = new Date();
	//	Date previousExecution = cron.getTimeBefore(today);

		log.info("Crawling web", dateFormat.format(new Date()));
	}


	private ZonedDateTime getPrevTime(CronExpression expression, ZonedDateTime nextTime, ZonedDateTime mayPrevTime) {
		ZonedDateTime start = mayPrevTime;
		ZonedDateTime end = nextTime;
		while (start.isBefore(end)) {
			ZonedDateTime middle = end.minusSeconds(
					TimeUnit.MILLISECONDS.toSeconds(Duration.between(start, end).toMillis() / 2));
			ZonedDateTime tmpNextTime = expression.next(middle);
			if (Objects.equals(tmpNextTime, nextTime)) {
				end = middle.minusSeconds(1);
			} else {
				start = middle.plusSeconds(1);
			}
		}
		return start;
	}


	@Scheduled(fixedRate = 10000)
	public void notifyUser() {
		log.info("Notifying user", dateFormat.format(new Date()));
	}
}
