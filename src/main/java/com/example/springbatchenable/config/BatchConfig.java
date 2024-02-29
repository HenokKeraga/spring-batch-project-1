package com.example.springbatchenable.config;

import com.example.springbatchenable.model.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
//@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    final JobRepository jobRepository;
    final PlatformTransactionManager platformTransactionManager;
    final DataSource dataSource;

    @Bean
    public Job job(Step step) {

        return new JobBuilder("job", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step step(ItemReader<Student> itemReader, ItemWriter<Student> itemWriter) {

        return new StepBuilder("step", jobRepository)
                .<Student, Student>chunk(2, platformTransactionManager)
                .reader(itemReader)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public ItemReader<Student> itemReader() {

        return new FlatFileItemReaderBuilder<Student>()
                .name("itemReader")
                .resource(new ClassPathResource("students.csv"))
                //  .lineMapper(new DefaultLineMapper())
                .delimited()
                .names("id", "name", "department", "age")
                .targetType(Student.class)
                .build();
    }

    @Bean
    public ItemWriter<Student> itemWriter() {

        return new JdbcBatchItemWriterBuilder<Student>()
                .dataSource(dataSource)
                .sql("INSERT INTO student (id,name,department,age) values (:id, :name, :department, :age)")
                .beanMapped()
                .build();
    }
}
