package org.id.bankspringbatch.config;

import lombok.Getter;
import org.id.bankspringbatch.domain.BankTransaction;
import org.id.bankspringbatch.repository.BankTransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig2 {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private ItemReader<BankTransaction> bankTransactionItemReader;
    @Autowired
    private ItemWriter<BankTransaction> bankTransactionItemWriter;
    //@Autowired
    //private ItemProcessor<BankTransaction, BankTransaction> bankTransactionItemProcessor;

    @Bean
    public Job bankJob(){
        Step step1 = stepBuilderFactory.get("step-load-data")
                .<BankTransaction,BankTransaction>chunk(100)
                .reader(bankTransactionItemReader)
                .processor(compositeItemProcessor())
                .writer(bankTransactionItemWriter)
                .build();
        return  jobBuilderFactory.get("bank-data-loader-job")
                .start(step1)
                .build();
    }

    @Bean
    public FlatFileItemReader<BankTransaction> flatFileItemReader(@Value("${inputFile}") Resource inputFile){
        FlatFileItemReader<BankTransaction> fileItemReader = new FlatFileItemReader<>();
        fileItemReader.setName("FFIR1");
        fileItemReader.setLinesToSkip(1);
        fileItemReader.setResource(inputFile);
        fileItemReader.setLineMapper(lineMapper());
        return fileItemReader;
    }
    @Bean
    public LineMapper<BankTransaction> lineMapper(){
        DefaultLineMapper<BankTransaction> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","accountID","strTransactionDate","transactionType","amount");
        lineMapper.setLineTokenizer(lineTokenizer);
        BeanWrapperFieldSetMapper fieldSetMapper = new BeanWrapperFieldSetMapper();
        fieldSetMapper.setTargetType(BankTransaction.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }
    //@Bean
    public ItemProcessor<BankTransaction,BankTransaction> itemProcessor(){
        return new ItemProcessor<BankTransaction, BankTransaction>() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm");
            @Override
            public BankTransaction process(BankTransaction bankTransaction) throws Exception {
                bankTransaction.setTransactionDate(simpleDateFormat.parse(bankTransaction.getStrTransactionDate()));
                return bankTransaction;
            }
        };
    }
    //@Bean
    public ItemProcessor<BankTransaction,BankTransaction> itemAnalyticsProcessor(){
        return new ItemProcessor<BankTransaction, BankTransaction>() {
            @Getter
            private double totalDebit;
            @Getter
            private double totalCredit;

            @Autowired
            private StateAnalyticsConfig stateAnalyticsConfig;

            @Override
            public BankTransaction process(BankTransaction bankTransaction) throws Exception {
                if(bankTransaction.getTransactionType().equals("D"))
                    totalDebit+=bankTransaction.getAmount();
                else if(bankTransaction.getTransactionType().equals("C"))
                    totalCredit+=bankTransaction.getAmount();
                System.out.println(stateAnalyticsConfig);
                System.out.println(stateAnalyticsConfig.state);
                stateAnalyticsConfig.state.put("totalDebit",totalDebit);
                stateAnalyticsConfig.state.put("totalCredit",totalCredit);
                return bankTransaction;
            }
        };
    }
    @Bean
    public ItemProcessor<BankTransaction,BankTransaction> itemProcessorBean(){
        return itemProcessor();
    }
    @Bean
    public ItemProcessor<BankTransaction,BankTransaction> itemAnalyticsProcessorBean(){
        return itemAnalyticsProcessor();
    }
    @Bean
    public CompositeItemProcessor<BankTransaction,BankTransaction> compositeItemProcessor(){
        List<ItemProcessor<BankTransaction,BankTransaction>> itemProcessors= new ArrayList<>();
        itemProcessors.add(itemProcessor());
        itemProcessors.add(itemAnalyticsProcessor());
        CompositeItemProcessor<BankTransaction,BankTransaction> compositeItemProcessor= new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(itemProcessors);
        return  compositeItemProcessor;

    }
    @Bean
    public ItemWriter<BankTransaction> itemWriter(){
       return new ItemWriter<BankTransaction>() {
           @Autowired
           private BankTransactionRepository bankTransactionRepository;
           @Override
           public void write(List<? extends BankTransaction> list) throws Exception {
                bankTransactionRepository.saveAll(list);
           }
       };
    }
}
