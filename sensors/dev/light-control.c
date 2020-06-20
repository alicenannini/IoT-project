#include "light-control.h"
#include "random.h"
#include <stdbool.h>

static double threshold = 40.0;
static double LIGHT_VALUE = 50;

double get_light_value(){

	return LIGHT_VALUE;

}

bool light_is_down_threshold(){

	return LIGHT_VALUE < threshold;

}

bool light_is_over_threshold(){

	return LIGHT_VALUE >= threshold;

}

/* Function to randomly generate a simulated value of the sensor */
double collect_light_data() {
	double maxValue = 100.00;
	double e = 15.0;
	double lowerBound = ((LIGHT_VALUE-e) < 0)? 0 : (-e);
	double upperBound = ((LIGHT_VALUE+e) > maxValue)? (LIGHT_VALUE+e-maxValue) : (e);
	
	/* generating a float in range lowerBound to upperBound */
	double delta = ((double)(random_rand()%(int)((upperBound-lowerBound)*maxValue)))/maxValue + lowerBound; 
	/* adding the delta to the old value to generate a realistic new value */
	double new_value = LIGHT_VALUE + delta ; //float in range 0 to 100
	LIGHT_VALUE = new_value;
	return new_value;
}
