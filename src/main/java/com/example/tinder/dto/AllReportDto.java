package com.example.tinder.dto;




public record AllReportDto (
     Long reportedId,
     String reportedUsername,
     String Email,
     String profilePic,
     String phoneNum,
     Long reportsCount)
{}

