package com.owenlow.analytics;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class AnalyticsReportingDemo {
    private static final String APPLICATION_NAME = "Hello Analytics Reporting";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Create a service app and generate a keyfile for it:
    private static final String KEY_FILE_LOCATION = "myserviceapp-123456-123456789abc.json";
    // View id for your app
    private static final String MYAPP_VIEWID = "123456789";

    public static void main(String[] args) {
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();

            GetReportsResponse response = getReport(service);
            printResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes an Analytics Reporting API V4 service object.
     *
     * @return An authorized Analytics Reporting API V4 service object.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream(KEY_FILE_LOCATION))
                .createScoped(AnalyticsReportingScopes.all());

        // Construct the Analytics Reporting service object.
        return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Queries the Analytics Reporting API V4.
     *
     * @param service An authorized Analytics Reporting API V4 service object.
     * @return GetReportResponse The Analytics Reporting API V4 response.
     * @throws IOException
     */
    private static GetReportsResponse getReport(AnalyticsReporting service) throws IOException {
        // Events & dimensions to pick from:
        // https://ga-dev-tools.appspot.com/dimensions-metrics-explorer/
        // Test queries here:
        // https://ga-dev-tools.appspot.com/query-explorer/

        /*
            Goal of this sample code is to retrieve "Item" events with action "Add to Order",
            and return their labels (which consists of the name of the item).
            On the client, this was logged as:

            ReactGA.event({
                category: 'Item',
                action: 'Add to Order',
                label: 'Pizza',
            });
         */

        // Create the DateRange object.
        DateRange dateRange = new DateRange()
                .setStartDate("7DaysAgo")
                .setEndDate("today");

        // Create the Metrics object, the top level data we want to search.
        // We logged an 'event' so search 'ga:totalEvents';
        Metric metric = new Metric().setExpression("ga:totalEvents");

        // Configure dimensions, the data we want to receive
        // In this case, we want the label, which we expect to be the name of an order item (ex. 'Pizza')
        Dimension eventLabel = new Dimension().setName("ga:eventLabel");

        // Configure filters, dimensions with values that we want to match.
        // Filter to only show "Item" events. May be unnecessary considering the implied context of addToOrderActionFilter
        DimensionFilter itemCategoryFilter = new DimensionFilter()
                .setDimensionName("ga:eventCategory")
                .setOperator("EXACT")
                .setExpressions(Arrays.asList("Item"));
        // Filter to only show addToOrder actions.
        DimensionFilter addToOrderActionFilter = new DimensionFilter()
                .setDimensionName("ga:eventAction")
                .setOperator("EXACT")
                .setExpressions(Arrays.asList("Add to Order"));
        // Not configured at the moment, but the client can be initialized with a custom dimension
        // In our case, this would allow us to filter by store
//        DimensionFilter locationDimensionFilter = new DimensionFilter()
//                .setDimensionName("ga:dimension1")
//                .setOperator("EXACT")
//                .setExpressions(Arrays.asList("fooStoreId"));

        // Create the ReportRequest object.
        // The report response will contain the dimension (label), and the count of times
        // that dimension + filter combination was logged. (Number of times the 'Pizza' 'Item' was 'Added to cart')
        ReportRequest request = new ReportRequest()
                .setViewId(MYAPP_VIEWID)
                .setDateRanges(Arrays.asList(dateRange))
                .setMetrics(Arrays.asList(metric))
                .setDimensions(Arrays.asList(eventLabel))
                .setDimensionFilterClauses(Arrays.asList(
                        new DimensionFilterClause()
                                .setOperator("AND")
                                .setFilters(Arrays.asList(
                                        itemCategoryFilter,
                                        addToOrderActionFilter
                                ))
                ));

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(Arrays.asList(request));

        // Call the batchGet method.
        GetReportsResponse response = service.reports().batchGet(getReport).execute();

        // Return the response.
        return response;
    }

    /**
     * Parses and prints the Analytics Reporting API V4 response.
     *
     * @param response An Analytics Reporting API V4 response.
     */
    private static void printResponse(GetReportsResponse response) {

        for (Report report: response.getReports()) {
            ColumnHeader header = report.getColumnHeader();
            List<String> dimensionHeaders = header.getDimensions();
            List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
            List<ReportRow> rows = report.getData().getRows();

            if (rows == null) {
                System.out.println("No data found for " + MYAPP_VIEWID);
                return;
            }

            for (ReportRow row: rows) {
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();

                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                    System.out.println(dimensionHeaders.get(i) + ": " + dimensions.get(i));
                }

                for (int j = 0; j < metrics.size(); j++) {
                    System.out.print("Date Range (" + j + "): ");
                    DateRangeValues values = metrics.get(j);
                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        System.out.println(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
                    }
                }
            }
        }
    }
}
