package org.id.bankspringbatch.controller;


import org.id.bankspringbatch.config.StateAnalyticsConfig;
import org.id.bankspringbatch.domain.BankTransaction;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class JobRestController {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private StateAnalyticsConfig stateAnalyticsConfig;

    @GetMapping("/startJob")
    public BatchStatus load() throws Exception {
        Map<String, JobParameter> params = new HashMap<>();
        params.put("time",new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(params);
        JobExecution jobExecution = jobLauncher.run(this.job, jobParameters);
        int i = 0;
        while(jobExecution.isRunning()){
            System.out.println(i+"...");
            i++;
        }
        return jobExecution.getStatus();
    }
    @GetMapping("/getState")
    public Map<String,Double> analytics() throws Exception {
        return StateAnalyticsConfig.state;
    }
}
